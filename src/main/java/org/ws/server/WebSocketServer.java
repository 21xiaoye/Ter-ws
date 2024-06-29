package org.ws.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ws.*;
import org.ws.draft.Draft;
import org.ws.exceptions.WrappedIOException;
import org.ws.framing.CloseFrame;
import org.ws.handshake.ClientHandshake;
import org.ws.handshake.HandshakeData;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 负责处理HTTP握手部分，具体实现由子类完成
 */
public abstract class WebSocketServer extends AbstractWebSocket implements Runnable{
    private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    private final Logger logger = LoggerFactory.getLogger(WebSocketServer.class);
    /**
     * 保存WebSocket握手成功的连接列表
     */
    private final Collection<WebSocket> connections;
    /**
     * WebSocket服务的端口号
     */
    private final InetSocketAddress address;
    /**
     * 此WebSocket服务的socket通道
     */
    private ServerSocketChannel server;

    /**
     * 从底层socket获取事件键
     */
    private Selector selector;
    /**
     * 服务器遵从的协议草案
     */
    private List<Draft> draftLists;
    private Thread selectorThread;
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    protected List<WebSocketWorker> decoders;
    private List<WebSocketImpl> webSocketImplList;
    private BlockingQueue<ByteBuffer> buffers;
    private int queueInvokes = 0;
    private final AtomicInteger queueSize =  new AtomicInteger(0);
    private WebSocketServerFactory webSocketServerFactory = new DefaultWebSocketServerFactory();

    public WebSocketServer(){
        this(new InetSocketAddress(WebSocketImpl.DEFAULT_PORT), AVAILABLE_PROCESSORS, null);
    }

    public WebSocketServer(InetSocketAddress address){
        this(address, AVAILABLE_PROCESSORS, null);
    }
    public WebSocketServer(InetSocketAddress address, int decoderCount){
        this(address, decoderCount,null);
    }
    public WebSocketServer(InetSocketAddress address, List<Draft> draftLists){
        this(address, AVAILABLE_PROCESSORS, draftLists);
    }
    public WebSocketServer(InetSocketAddress address, int decoderCount, List<Draft> draftLists){
        this(address, decoderCount, draftLists, new HashSet<WebSocket>());
    }

    public WebSocketServer(InetSocketAddress address, int decoderCount, List<Draft> draftLists, Collection<WebSocket> webSocketCollection){
        if(Objects.isNull(address) || decoderCount < 1 || Objects.isNull(webSocketCollection)){
            throw new IllegalArgumentException("address add webSocketCollection must not be null and you need at least 1 decoder");
        }
        if(Objects.isNull(draftLists)){
            this.draftLists = Collections.emptyList();
        }else{
            this.draftLists = draftLists;
        }
        this.address = address;
        this.connections = webSocketCollection;
        webSocketImplList = new LinkedList<>();
        decoders = new ArrayList<>(decoderCount);
        buffers = new LinkedBlockingDeque<>();
        for (int i = 0; i < decoderCount; i++) {
            WebSocketWorker webSocketWorker = new WebSocketWorker();
            decoders.add(webSocketWorker);
        }
    }

    /**
     * 启动服务器选择器线程，
     * 该线程绑定到当前设置的端口号和 WebSocket 连接请求的侦听器。
     * 创建一个大小为{@link WebSocketServer#AVAILABLE_PROCESSORS}的固定线程池
     */
    public void start(){
        if(Objects.nonNull(selectorThread)){
            throw new IllegalStateException(getClass().getName()+" can only be started once");
        }
        Thread thread = new Thread(this);
        thread.start();
    }

    public void stop(int timeout) throws InterruptedException {
        stop(timeout, "");
    }
    public void stop() throws InterruptedException {
        stop(0);
    }
    public void stop(int timeout, String closeMessage) throws InterruptedException{
        if(!isClosed.compareAndSet(false,true)){
            return;
        }

        List<WebSocket> socketsToClose;
        synchronized (connections){
            socketsToClose = new ArrayList<>(connections);
        }
        // 关闭所有客户端连接
        for (WebSocket webSocket : socketsToClose){
            webSocket.close(CloseFrame.GOING_AWAY, closeMessage);
        }
        //关闭底层 ServerSocketChannel, selectorThread
        synchronized (this){
            if(Objects.nonNull(selectorThread) && Objects.nonNull(selector)){
                selector.wakeup();
                selectorThread.join(timeout);
            }
        }
    }

    /**
     * 返回所有客户端连接
     *
     * @returnconnections
     */
    public Collection<WebSocket> getConnections(){
        synchronized (connections){
            return Collections.unmodifiableCollection(new ArrayList<>(connections));
        }
    }

    public InetSocketAddress getAddress(){
        return this.address;
    }

    public int getPort(){
        int port = getAddress().getPort();
        if(port == 0 && Objects.nonNull(server)){
            port = server.socket().getLocalPort();
        }
        return port;
    }

    public List<Draft> getDraftLists(){
        return Collections.unmodifiableList(draftLists);
    }

    @Override
    public void onWebsocketMessage(WebSocket conn, String message) {
        onMessage(conn, message);
    }

    @Override
    public void onWebSocketMessage(WebSocket conn, ByteBuffer byteBuffer) {
        onMessage(conn, byteBuffer);
    }

    @Override
    public void onWebSocketOpen(WebSocket conn, HandshakeData handshakeData) {
        if(addConnection(conn)){
            onOpen(conn, (ClientHandshake) handshakeData);
        }
    }

    @Override
    public void onWebsocketError(WebSocket conn, Exception ex) {
        onError(conn, ex);
    }


    @Override
    public InetSocketAddress getLocalSocketAddress(WebSocket conn) {
        return (InetSocketAddress) getSocket(conn).getLocalSocketAddress();
    }

    @Override
    public InetSocketAddress getRemoteSocketAddress(WebSocket conn) {
        return (InetSocketAddress) getSocket(conn).getRemoteSocketAddress();
    }

    private Socket getSocket(WebSocket conn){
        WebSocketImpl webSocketImpl = (WebSocketImpl) conn;
        return ((SocketChannel) webSocketImpl.getSelectionKey().channel()).socket();
    }

    @Override
    public void onWriteDemand(WebSocket conn) {
        WebSocketImpl webSocket = (WebSocketImpl) conn;
        try {
            ((WebSocketImpl) conn).getSelectionKey().interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        }catch (CancelledKeyException exception){
            webSocket.outQueue.clear();
        }
        selector.wakeup();
    }
    @Override
    public final void onWebSocketClose(WebSocket conn, int code, String reason, boolean remote) {

    }
    @Override
    public void onWebSocketClosing(WebSocket conn, int code, String reason, boolean remote) {

    }
    @Override
    public void onWebSocketCloseInitiated(WebSocket ws, int code, String reason) {

    }
    /**
     * 握手成功之后调用
     *
     * @param conn
     * @param clientHandshake
     */
    public abstract void onOpen(WebSocket conn, ClientHandshake clientHandshake);

    /**
     * 连接关闭
     *
     * @param conn
     * @param code
     * @param reason
     * @param remote
     */
    public abstract void onClose(WebSocket conn, int code, String reason, boolean remote);

    /**
     * 处理接收消息
     *
     * @param conn
     * @param message
     */
    public abstract void onMessage(WebSocket conn, String message);
    public abstract void onMessage(WebSocket conn, ByteBuffer byteBuffer);

    /**
     * WebSocket服务器启动时调用
     */
    public abstract void onStart();

    /**
     * 发生错误时调用
     *
     * @param conn
     * @param exception
     */
    public abstract void onError(WebSocket conn, Exception exception);
    public void run(){
        if(!doEnsureSingleThread()){
            return;
        }
        if(!doSetupSelectorAndServerThread()){
            return;
        }
        try {
            int shutdownCount = 5;
            int selectTimeout = 0;
            while (!selectorThread.isInterrupted() && shutdownCount !=0){
                SelectionKey key = null;
                try {
                    if(isClosed.get()){
                        selectTimeout = 5;
                    }

                    int keyCount = selector.select(selectTimeout);
                    if(keyCount == 0 && isClosed.get()){
                        shutdownCount--;
                    }

                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    while (iterator.hasNext()){
                        key = iterator.next();
                        if(!key.isValid()){
                            continue;
                        }
                        if(key.isAcceptable()){
                            doAccept(key, iterator);
                            continue;
                        }
                        if(key.isReadable() && !doRead(key, iterator)){
                            continue;
                        }
                        if(key.isWritable()){
                            doWrite(key);
                        }
                        doAdditionalRead();
                    }
                }catch (CancellationException cancellationException){

                }catch (ClosedByInterruptException closedByInterruptException){
                    return;
                }catch (WrappedIOException wrappedIOException){
                    handleIOException(key, wrappedIOException.getWebSocket(), wrappedIOException.getIoException());
                }catch (IOException ioException){
                    handleIOException(key, null, ioException);
                }catch (InterruptedException interruptedException){
                    Thread.currentThread().interrupt();
                }
            }
        }catch (RuntimeException exception){
            handleFatal(null, exception);
        }finally {
            logger.error("server and resource close.....");
        }
    }
    private boolean doEnsureSingleThread(){
        synchronized (this){
            if(Objects.nonNull(selectorThread)){
                throw new IllegalStateException(getClass().getName() + " can only be started once.");
            }
            selectorThread = Thread.currentThread();
            if(isClosed.get()){
                return false;
            }
        }
        return true;
    }

    /**
     * 设置选择器线程以及服务器设置
     *
     * @return
     */
    private boolean doSetupSelectorAndServerThread(){
        selectorThread.setName("WebSocketSelector-"+selectorThread.getId());
        try {
            server = ServerSocketChannel.open();
            server.configureBlocking(false);
            ServerSocket socket = server.socket();
            socket.bind(address);
            selector = Selector.open();
            server.register(selector, server.validOps());
            for (WebSocketWorker webSocketWorker : decoders){
                webSocketWorker.start();
            }
            onStart();
        }catch (IOException exception){
            handleFatal(null, exception);
            return false;
        }
        return true;
    }

    protected boolean addConnection(WebSocket webSocket){
        if(!isClosed.get()){
            synchronized (connections){
                return this.connections.add(webSocket);
            }
        }else {
            webSocket.close(CloseFrame.GOING_AWAY);
            return false;
        }
    }

    private void handleFatal(WebSocket conn, Exception exception){
        logger.error("Shutdown due to fatal error", exception);
        onError(conn, exception);

        String causeMessage = Objects.nonNull(exception.getCause()) ? "caused by"+ exception.getMessage().getClass().getName() : "";
        String errorMessage = "Got error on server side:" + exception.getClass().getName() + causeMessage;

        try {
            this.stop(0, errorMessage);
        }catch (InterruptedException ex){
            Thread.currentThread().interrupt();
            logger.error("Interrupt during stop", 0);
            onError(null, ex);
        }
    }
    private void handleIOException(SelectionKey key, WebSocket conn, IOException ex) {
        if (key != null) {
            key.cancel();
        }
        if (conn != null) {
            conn.closeConnection(CloseFrame.ABNORMAL_CLOSE, ex.getMessage());
        } else if (key != null) {
            SelectableChannel channel = key.channel();
            if (channel != null && channel
                    .isOpen()) {
                try {
                    channel.close();
                } catch (IOException e) {
                }
                logger.trace("Connection closed because of exception", ex);
            }
        }
    }
    private void doAccept(SelectionKey selectionKey, Iterator<SelectionKey> iterator)
            throws IOException, InterruptedException{
        if(!onConnect(selectionKey)){
            selectionKey.cancel();
            return;
        }
        SocketChannel channel = this.server.accept();
        if(Objects.isNull(channel)){
            return;
        }
        channel.configureBlocking(false);
        Socket socket = channel.socket();
        socket.setKeepAlive(true);
        WebSocketImpl webSocketImpl = webSocketServerFactory.createdWebSocket(this, draftLists);
        webSocketImpl.setSelectionKey(channel.register(selector, SelectionKey.OP_READ, webSocketImpl));

        webSocketImpl.setChannel(channel);
        iterator.remove();
        allocateBuffers(webSocketImpl);
    }

    private boolean doRead(SelectionKey selectionKey, Iterator<SelectionKey> iterator)
            throws InterruptedException, WrappedIOException{
        WebSocketImpl conn = (WebSocketImpl)selectionKey.attachment();
        ByteBuffer buffer = takeBuffer();
        if(Objects.isNull(conn.getChannel())){
            selectionKey.cancel();
            handleIOException(selectionKey, conn, new IOException());
            return false;
        }
        try {
            if(SocketChannelIOHelper.read(buffer, conn, conn.getChannel())){
                if(buffer.hasRemaining()){
                    conn.inQueue.put(buffer);
                    queue(conn);
                    iterator.remove();
                    if(conn.getChannel() instanceof  WrappedByteChannel &&
                            ((WrappedByteChannel)conn.getChannel()).isNeedRead()){
                        webSocketImplList.add(conn);
                    }
                }else{
                    pushBuffer(buffer);
                }
            }else{
                pushBuffer(buffer);
            }
        }catch (IOException exception){
            pushBuffer(buffer);
            throw new WrappedIOException(conn, exception);
        }
        return true;
    }
    private void doWrite(SelectionKey selectionKey) throws WrappedIOException {
        WebSocketImpl conn = (WebSocketImpl) selectionKey.attachment();
        try {
            if (SocketChannelIOHelper.batch(conn, conn.getChannel()) && selectionKey.isValid()) {
                selectionKey.interestOps(SelectionKey.OP_READ);
            }
        }catch (IOException exception){
            throw new WrappedIOException(conn, exception);
        }

    }
    private void doAdditionalRead() throws InterruptedException, IOException{
        WebSocketImpl conn;
        while (!webSocketImplList.isEmpty()){
            conn = webSocketImplList.remove(0);
            conn.getChannel();
        }
    }
    protected void allocateBuffers(WebSocket webSocket) throws InterruptedException{
        if(queueSize.get() >= 2 * decoders.size()+1){
            return;
        }
        queueSize.incrementAndGet();
        buffers.put(createBuffer());
    }
    public ByteBuffer createBuffer(){
        return ByteBuffer.allocate(DEFAULT_READ_BUFFER_SIZE);
    }
    /**
     * 是否接收连
     *
     * @param key
     * @return
     */
    protected boolean onConnect(SelectionKey key) {
        return true;
    }
    protected void queue(WebSocketImpl webSocketImpl) throws InterruptedException{
        if(Objects.isNull(webSocketImpl.getWorkerThread())){
            webSocketImpl.setWorkerThread(decoders.get(queueInvokes % decoders.size()));
            queueInvokes++;
        }
        webSocketImpl.getWorkerThread().put(webSocketImpl);
    }
    private ByteBuffer takeBuffer() throws InterruptedException{
        return buffers.take();
    }
    private void pushBuffer(ByteBuffer buffer) throws InterruptedException{
        if(buffers.size() > queueSize.intValue()){
            return;
        }
        buffers.put(buffer);
    }
    public class WebSocketWorker extends Thread{
        private BlockingQueue<WebSocketImpl> webSocketBlockingQueue;

        public WebSocketWorker(){
            webSocketBlockingQueue = new LinkedBlockingDeque<>();
            setName("WebSocketWorker-"+getId());
            setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    logger.error("Uncaught exception in thread {} : {}", t.getName(), e);
                }
            });
        }

        public void put(WebSocketImpl webSocket) throws InterruptedException{
            webSocketBlockingQueue.put(webSocket);
        }

        @Override
        public void run() {
            WebSocketImpl webSocket = null;
            try {
                while (true){
                    ByteBuffer byteBuffer;
                    webSocket = webSocketBlockingQueue.take();
                    byteBuffer = webSocket.inQueue.poll();
                    assert (Objects.nonNull(buffers));
                    doDecode(webSocket, byteBuffer);
                    webSocket = null;
                }
            }catch (InterruptedException e){
                Thread.currentThread().interrupt();
            }catch (Throwable e){
                logger.error("Uncaught exception in thread {} : {}", getName(), e);
                if(Objects.nonNull(webSocket)){
                    Exception exception = new Exception();
                    onWebsocketError(webSocket, exception);
                    webSocket.close();
                }
            }
        }

        private void doDecode(WebSocketImpl webSocket, ByteBuffer buffer) throws  InterruptedException{
            try {
                webSocket.decode(buffer);
            }catch (Exception e){
                logger.error("Error while reading from remote connection",e);
            }finally {
                pushBuffer(buffer);
            }
        }
    }
}
