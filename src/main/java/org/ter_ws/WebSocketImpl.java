package org.ter_ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ter_ws.enums.ReadyState;
import org.ter_ws.framing.FrameData;
import org.ter_ws.protocols.IProtocol;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

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
     * 连接状态
     */
    private volatile ReadyState readyState = ReadyState.NOT_YET_CONNECTED;
    public final BlockingQueue<ByteBuffer> inQueue;

    public WebSocketImpl() {
        inQueue = new LinkedBlockingDeque<>();
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
}
