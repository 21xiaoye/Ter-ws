package org.ter_ws.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ter_ws.AbstractWebSocket;
import org.ter_ws.WebSocket;
import org.ter_ws.WebSocketImpl;
import org.ter_ws.drafts.Draft;
import org.ter_ws.framing.CloseFrame;
import sun.plugin.dom.exception.InvalidStateException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.*;
import java.util.concurrent.BlockingQueue;
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
    private int queueInvoke = 0;
    private final AtomicInteger queueSize =  new AtomicInteger(0);

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
            throw new InvalidStateException(getClass().getName()+" can only be started once");
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
     * @return
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

    public abstract void onStart();
    public void run(){
        if(!doEnsureSingleThread()){
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
                }catch (Exception exception){

                }
            }
        }
    }
    private boolean doEnsureSingleThread(){
        synchronized (this){
            if(Objects.nonNull(selectorThread)){
                throw new InvalidStateException(getClass().getName() + " can only be started once.");
            }
            selectorThread = Thread.currentThread();
            if(isClosed.get()){
                return false;
            }
        }
        return true;
    }

    private boolean doSetupSelectorAndServerThread(){
        selectorThread.setName("WebSocketSelector-"+selectorThread.getId());
        try {
            server = ServerSocketChannel.open();
            server.configureBlocking(false);
            ServerSocket socket = server.socket();

            selector = Selector.open();
            server.register(selector, server.validOps());
            for (WebSocketWorker webSocketWorker : decoders){
                webSocketWorker.start();
            }
            onStart();

        }catch (IOException exception){
            return false;
        }
        return true;
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

        public void put(WebSocketImpl webSocket) throws  InterruptedException{
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

            }
        }
    }




























}
