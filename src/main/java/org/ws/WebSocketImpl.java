package org.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ws.draft.Draft;
import org.ws.draft.Draft_6455;
import org.ws.enums.ReadyState;
import org.ws.enums.Role;
import org.ws.framing.FrameData;
import org.ws.handshake.ClientHandshake;
import org.ws.protocols.IProtocol;
import org.ws.server.WebSocketServer;

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
    private List<Draft> draftList;
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
            draftList = new ArrayList<>();
            draftList.add(new Draft_6455());
        }else{
            this.draftList = draftList;
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

            }
        }else{
//            if()
        }
    }

//    private void

    @Override
    public void close(int code, String message) {

    }

    @Override
    public void close(int code) {

    }

    @Override
    public void close() {

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
        return null;
    }

    @Override
    public InetSocketAddress getLocalSocketAddress() {
        return null;
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
