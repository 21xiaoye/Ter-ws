package org.ws;

import org.ws.enums.ReadyState;
import org.ws.framing.FrameData;
import org.ws.protocols.IProtocol;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public interface WebSocket {
    /**
     *  处理握手结束事件
     *
     * @param code      关闭标识
     * @param message   关闭信息
     */
    void close(int code, String message);

    /**
     * 处理握手结束事件
     *
     * @param code  关闭标识
     */
    void close(int code);

    /**
     * 处理握手结束事件
     */
    void close();

    /**
     * 立即关闭连接
     *
     * @param code
     * @param message
     */

    void closeConnection(int code, String message);

    /**
     * 发送文本数据
     *
     * @param text
     */
    void send(String text);

    /**
     * 发送二进制数据
     *
     * @param byteBuffer
     */
    void send(ByteBuffer byteBuffer);

    /**
     * 发送二进制数据
     *
     * @param bytes
     */
    void send(byte[] bytes);

    /**
     * 发送帧数据
     *
     * @param frameData
     */
    void sendFrame(FrameData frameData);

    /**
     * 向另一端ping
     */
    void sendPing();

    /**
     * 检查是否具有缓冲数据
     * @return
     */
    boolean hasBufferedData();

    /**
     * 获取远程连接地址信息
     *
     * @return
     */
    InetSocketAddress getRemoteSocketAddress();

    /**
     *获取远程连接地址信息
     *
     * @return
     */
    InetSocketAddress getLocalSocketAddress();

    /**
     * 检查连接状态是否为OPEN
     * @see org.ws.enums.ReadyState
     *
     * @return
     */
    boolean isOpen();

    /**
     * 检查连接状态是否为 CLOSING
     * @see org.ws.enums.ReadyState
     * @return
     */
    boolean isClosing();

    /**
     * 检查连接状态是否为CLOSED
     * @see org.ws.enums.ReadyState
     *
     * @return
     */
    boolean isClosed();

    /**
     * 获取WebSocket连接状态
     *
     * @return
     */
    ReadyState getReadyState();

    /**
     * 返回WebSocket需要的Sec-WebSocket-Protocol
     *
     * @return
     */
    IProtocol getProtocol();
}
