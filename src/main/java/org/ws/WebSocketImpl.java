package org.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ws.draft.Draft;
import org.ws.draft.Draft_6455;
import org.ws.enums.HandshakeState;
import org.ws.enums.ReadyState;
import org.ws.enums.Role;
import org.ws.exceptions.InvalidDataException;
import org.ws.exceptions.InvalidHandshakeException;
import org.ws.framing.CloseFrame;
import org.ws.framing.FrameData;
import org.ws.handshake.ClientHandshake;
import org.ws.handshake.HandshakeData;
import org.ws.handshake.ServerHandshake;
import org.ws.handshake.ServerHandshakeBuilder;
import org.ws.protocols.IProtocol;
import org.ws.server.WebSocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class WebSocketImpl implements WebSocket{
    /**
     * WebSocket默认端口
     */
    public static final int DEFAULT_PORT= 80;

    /**
     * 日志实例
     */
    private final Logger logger = LoggerFactory.getLogger(WebSocketImpl.class);
    /**
     * 输出缓冲区，服务端发送到客户端
     */
    public final BlockingQueue<ByteBuffer> outQueue;
    /**
     * 输入缓冲区，客户端发送到服务端
     */
    public final BlockingQueue<ByteBuffer> inQueue;
    private final WebSocketListener webSocketListener;
    private SelectionKey selectionKey;
    private ByteChannel channel;
    private WebSocketServer.WebSocketWorker workerThread;
    private boolean flushAndCloseState = false;
    /**
     * 连接状态
     */
    private volatile ReadyState readyState = ReadyState.NOT_YET_CONNECTED;
    private List<Draft> knowDraft;
    private Draft draft = null;
    private Role role;

    private ByteBuffer tmpHandshakeBytes = ByteBuffer.allocate(0);
    private ClientHandshake clientHandshake = null;
    private String closeMessage = null;
    private Integer closeCode = null;
    private Boolean closedRemotely = null;
    private String resourceDescriptor = null;
    private long lastPong = System.nanoTime();
    private final Object synchronizerWriteObject = new Object();
    public WebSocketImpl(WebSocketListener webSocketListener, List<Draft> draftList) {
        this(webSocketListener, (Draft) null);
        this.role = Role.SERVER;
        if(Objects.isNull(draftList) || draftList.isEmpty()){
            this.knowDraft = new ArrayList<>();
            this.knowDraft.add(new Draft_6455());
        }else{
            this.knowDraft = draftList;
        }
    }
    public WebSocketImpl(WebSocketListener webSocketListener, Draft draft){
        if(Objects.isNull(webSocketListener) || (Objects.isNull(draft) && role == Role.SERVER)){
            throw new IllegalArgumentException("parameters must not be null");
        }

        this.outQueue = new LinkedBlockingQueue<>();
        this.inQueue = new LinkedBlockingQueue<>();
        this.webSocketListener = webSocketListener;
        this.role = Role.CLIENT;
        if(Objects.nonNull(draft)){
            this.draft = draft.copyInstance();
        }
    }

    public void decode(ByteBuffer socketBuffer){
        assert (socketBuffer.hasRemaining());
        if(logger.isTraceEnabled()){
            logger.trace("process({}):({})", socketBuffer.remaining(),
                    (socketBuffer.remaining() > 1000 ? "too big to display"
                            : new String(socketBuffer.array(), socketBuffer.position(), socketBuffer.remaining())));
        }

        if(readyState != ReadyState.NOT_YET_CONNECTED){
            if(readyState == ReadyState.OPEN){
                decodeFrame(socketBuffer);
            }
        }else{
            if(decodeHandshake(socketBuffer) && (!isClosing() && !isClosed())){
                assert (tmpHandshakeBytes.hasRemaining() != socketBuffer.hasRemaining() || !socketBuffer.hasRemaining());
                if(socketBuffer.hasRemaining()){
                    decodeFrame(socketBuffer);
                }
                if(tmpHandshakeBytes.hasRemaining()){
                    decodeFrame(tmpHandshakeBytes);
                }
            }
        }
    }

    private boolean decodeHandshake(ByteBuffer socketBufferNew){
        ByteBuffer socketBuffer;
        if(tmpHandshakeBytes.capacity() == 0){
            socketBuffer = socketBufferNew;
        }else{
            if(tmpHandshakeBytes.remaining() < socketBufferNew.remaining()){
                ByteBuffer buffer = ByteBuffer.allocate(tmpHandshakeBytes.capacity() + socketBufferNew.remaining());
                tmpHandshakeBytes.flip();
                buffer.put(tmpHandshakeBytes);
                tmpHandshakeBytes = buffer;
            }
            tmpHandshakeBytes.put(socketBufferNew);
            tmpHandshakeBytes.flip();
            socketBuffer = tmpHandshakeBytes;
        }
        socketBuffer.mark();
        try {
            HandshakeState handshakeState;
            try {
                if(role == Role.SERVER){
                    if(Objects.isNull(draft)){
                        for (Draft d : knowDraft) {
                            d = d.copyInstance();
                            try {
                                d.setRole(role);
                                socketBuffer.reset();
                                HandshakeData handshakeData =  d.translateHandshake(socketBuffer);
                                if(!(handshakeData instanceof ClientHandshake)){
                                    logger.trace("Closing due to wrong handshake");
                                    return false;
                                }
                                ClientHandshake handshake = (ClientHandshake) handshakeData;
                                handshakeState = d.acceptHandshakeAsServer(handshake);
                                if(handshakeState == HandshakeState.MATCHED){
                                    resourceDescriptor = handshake.getResourceDescriptor();
                                    ServerHandshakeBuilder response;
                                    try {
                                        response = webSocketListener.onWebSocketHandshakeReceivedAsServer(this, d, handshake);
                                    }catch (InvalidDataException exception){
                                        logger.trace("Closing due to wrong handshake, Possible handshake rejection",exception);
                                        return false;
                                    }catch (RuntimeException exception){
                                        logger.error("Closing due to internal server error",exception);
                                        webSocketListener.onWebsocketError(this, exception);
                                        return false;
                                    }
                                    write(d.createHandshake(d.postProcessHandshakeResponseAsServer(handshake, response)));
                                    draft = d;
                                    open(handshake);
                                    return true;
                                }
                            }catch (InvalidHandshakeException exception){
                                logger.info("go on with an other draft");
                            }
                        }
                        if(Objects.isNull(draft)){
                            logger.trace("Closing due to protocol error: no draft matches");
                            return false;
                        }
                    } else{
                        HandshakeData handshakeData = draft.translateHandshake(socketBuffer);
                        if(!(handshakeData instanceof  ClientHandshake)){
                            logger.trace("Closing due to protocol error: wrong http function");
                            flushAndClose(CloseFrame.PROTOCOL_ERROR, "wrong http function", false);
                            return false;
                        }
                        ClientHandshake handshake = (ClientHandshake) handshakeData;
                        handshakeState = draft.acceptHandshakeAsServer(handshake);
                        if(handshakeState == HandshakeState.MATCHED){
                            return true;
                        }else {
                            logger.trace("Closing due to protocol error: the handshake did finally not match");
                        }
                        return false;
                    }
                } else if (role == Role.CLIENT) {
                    draft.setRole(role);
                    HandshakeData handshakeData = draft.translateHandshake(socketBuffer);
                    if(!(handshakeData instanceof ServerHandshake)){
                        logger.trace("Closing due to protocol error: wrong http function");
                        flushAndClose(CloseFrame.PROTOCOL_ERROR, "wrong http function", false);
                        return false;
                    }
                    ServerHandshake handshake = (ServerHandshake) handshakeData;
                    handshakeState = draft.acceptHandshakeAsClient(clientHandshake ,handshake);
                    if(handshakeState == HandshakeState.MATCHED){
                        try {
                            webSocketListener.onWebsocketHandshakeReceivedAsClient(this, clientHandshake, handshake);
                        }catch (InvalidDataException exception){
                            logger.trace("Closing due to invalid data exception. Possible handshake rejection",exception);
                            flushAndClose(exception.getCloseCode(), exception.getMessage(), false);
                            return false;
                        }catch (RuntimeException exception){
                            logger.error("Closing due to invalid data exception. Possible handshake rejection",exception);
                            webSocketListener.onWebsocketError(this, exception);
                            flushAndClose(CloseFrame.NEVER_CONNECTED, exception.getMessage(), false);
                            return false;
                        }
                    }
                }
            }catch (InvalidHandshakeException exception){
                logger.trace("Closing due to invalid handshake",exception);
                close(exception);
            }
        }catch (Exception exception){
            close(exception);
        }
        return false;
    }
    private void decodeFrame(ByteBuffer buffer){
        List<FrameData> frameData;
        try {
            frameData = draft.translateFrame(buffer);
            for (FrameData frame : frameData){
                logger.trace("matched frame:{}",frame);
                draft.processFrame(this, frame);
            }
        }catch (InvalidDataException exception){
            logger.error("Closing due to invalid data in frame");
            webSocketListener.onWebsocketError(this, exception);
            close(exception);
        }catch (Error error){
            Exception exception = new Exception(error);
            webSocketListener.onWebsocketError(this, exception);
        }
    }
    public synchronized void close(int code, String message, boolean remote){
        if(readyState != ReadyState.CLOSING && readyState != ReadyState.CLOSED){
            if(readyState == ReadyState.OPEN){
                if(code == CloseFrame.ABNORMAL_CLOSE){
                    assert (!remote);
                    readyState = ReadyState.CLOSING;
                    flushAndClose(code, message, false);
                    return;
                }
            }
            readyState = ReadyState.CLOSING;
            tmpHandshakeBytes = null;
        }
    }
    @Override
    public void close(int code, String message) {

    }

    @Override
    public void close(int code) {

    }
    public void close(Exception exception){}


    @Override
    public void close() {

    }

    public synchronized void flushAndClose(int code, String message, boolean remote){
        if(flushAndCloseState){
            return;
        }
        closeCode = code;
        closeMessage = message;
        closedRemotely = remote;
        flushAndCloseState = true;
        webSocketListener.onWriteDemand(this);

        try {
            webSocketListener.onWebSocketClosing(this, code, message, remote);
        }catch (RuntimeException exception){
            logger.error("Exception in onWebSocketClosing");
            webSocketListener.onWebsocketError(this, exception);
        }
        if(Objects.nonNull(draft)){
            draft.reset();
        }
        clientHandshake = null;
    }
    public synchronized void closeConnection(int code, String message, boolean remote){
        if(readyState == ReadyState.CLOSED){
            return;
        }
        if(readyState == ReadyState.OPEN){
            if(code == CloseFrame.ABNORMAL_CLOSE){
                readyState = ReadyState.CLOSING;
            }
        }
        if(Objects.nonNull(selectionKey)){
            selectionKey.channel();
        }
        if(Objects.nonNull(channel)){
            try {
                channel.close();
            }catch (IOException exception){
                if(Objects.nonNull(exception.getMessage())&& "Broken pipe".equals(exception.getMessage())){
                    logger.trace("Caught IOException: Broken pipe during closeConnection",exception);
                }else{
                    logger.error("Exception during channel.close()",exception);
                    webSocketListener.onWebsocketError(this,exception);
                }
            }
        }

        try {
            this.webSocketListener.onWebSocketClose(this, code, message, remote);
        }catch (RuntimeException exception){
            webSocketListener.onWebsocketError(this, exception);
        }
        if(Objects.nonNull(draft)){
            draft.reset();
        }
        clientHandshake = null;
        readyState = ReadyState.CLOSED;
    }
    @Override
    public void closeConnection(int code, String message) {

    }
    public void closeConnection() {

    }

    @Override
    public void send(String text) {

    }

    @Override
    public void send(ByteBuffer byteBuffer) {

    }

    @Override
    public void send(byte[] bytes) {

    }

    @Override
    public void sendFrame(FrameData frameData) {

    }

    @Override
    public void sendPing() {

    }

    @Override
    public boolean hasBufferedData() {
        return false;
    }

    @Override
    public InetSocketAddress getRemoteSocketAddress() {
        return webSocketListener.getRemoteSocketAddress(this);
    }

    @Override
    public InetSocketAddress getLocalSocketAddress() {
        return null;
    }

    private void open(HandshakeData handshakeData){
        logger.trace("open using draft:{}", draft);
        readyState = ReadyState.OPEN;
        updateLastPong();
        try {
            webSocketListener.onWebSocketOpen(this, handshakeData);
        }catch (RuntimeException exception){
            webSocketListener.onWebsocketError(this, exception);
        }
    }
    public void updateLastPong() {
        this.lastPong = System.nanoTime();
    }

    private void write(List<ByteBuffer> buffers){
        synchronized (synchronizerWriteObject){
            for (ByteBuffer buffer : buffers){
                write(buffer);
            }
        }
    }

    private void write(ByteBuffer buffer){
        logger.trace("write({}):{}", buffer.remaining(),
                buffer.remaining() > 1000 ? "too big to display" : new String(buffer.array()));
        outQueue.add(buffer);
        webSocketListener.onWriteDemand(this);
    }
    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public boolean isClosing() {
        return false;
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public ReadyState getReadyState() {
        return null;
    }

    @Override
    public <T> void setAttachment(T attachment) {

    }
    @Override
    public <T> T getAttachment() {
        return null;
    }

    @Override
    public IProtocol getProtocol() {
        return null;
    }

    public SelectionKey getSelectionKey() {
        return selectionKey;
    }
    public void setSelectionKey(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
    }

    public ByteChannel getChannel() {
        return channel;
    }

    public void setChannel(ByteChannel channel) {
        this.channel = channel;
    }
    public WebSocketServer.WebSocketWorker getWorkerThread() {
        return workerThread;
    }

    public void setWorkerThread(WebSocketServer.WebSocketWorker workerThread) {
        this.workerThread = workerThread;
    }
}
