package org.ws;


import org.ws.draft.Draft;
import org.ws.exceptions.InvalidDataException;
import org.ws.framing.FrameData;
import org.ws.framing.PingFrame;
import org.ws.handshake.ClientHandshake;
import org.ws.handshake.HandshakeData;
import org.ws.handshake.ServerHandshake;
import org.ws.handshake.ServerHandshakeBuilder;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public interface WebSocketListener {
    /**
     * 首次与WebSocket服务器建立连接时且完成握手操作，由服务端调用
     * 服务端可设置连接条件
     *
     * @param conn
     * @param draft
     * @param request
     * @return
     * @throws com.sun.media.sound.InvalidDataException
     */
    ServerHandshakeBuilder onWebSocketHandshakeReceivedAsServer(WebSocket conn, Draft draft, ClientHandshake request) throws InvalidDataException;

    /**
     * 首次建立WebSocket连接且收到WebSocket服务端响应，由客户端调用
     *
     * @param conn
     * @param request
     * @param response
     * @throws com.sun.media.sound.InvalidDataException
     */
    void onWebsocketHandshakeReceivedAsClient(WebSocket conn, ClientHandshake request, ServerHandshake response) throws InvalidDataException;

    /**
     * 首次建立WebSocket连接且发送握手请求，由客户端调用
     *
     * @param conn
     * @param request
     * @throws com.sun.media.sound.InvalidDataException
     */
    void onWebsocketHandshakeSentAsClient(WebSocket conn, ClientHandshake request) throws InvalidDataException;

    /**
     * 收到文本数据时调用
     *
     * @param conn
     * @param message
     */
    void onWebsocketMessage(WebSocket conn, String message);

    /**
     * 收到二进制数据时调用
     *
     * @param conn
     * @param byteBuffer
     */
    void onWebSocketMessage(WebSocket conn, ByteBuffer byteBuffer);

    /**
     * 建立完整连接时调用
     *
     * @param conn
     * @param handshakeData
     */
    void onWebSocketOpen(WebSocket conn, HandshakeData handshakeData);

    /**
     * 显示调用或者客户端连接中断时调用
     *
     * @param conn
     * @param handshakeData
     */
    void onWebSocketClose(WebSocket conn, HandshakeData handshakeData);

    /**
     *
     *
     * @param conn
     * @param code
     * @param reason
     * @param remote
     */
    void onWebSocketClosing(WebSocket conn, int code, String reason, boolean remote);

    /**
     * WebSocket服务端关闭连接
     *
     * @param ws
     * @param code  关闭状态码
     * @param reason    关闭原因
     */
    void onWebSocketCloseInitiated(WebSocket ws, int code, String reason);
    /**
     * 连接发生错误时调用，如果因为错误导致连接失败或者中断将调用onWebSocketClose();
     *
     * @param conn
     * @param ex
     */
    void onWebsocketError(WebSocket conn, Exception ex);

    /**
     * 收到ping frame时调用，返回pong frame作为响应
     * @param conn
     * @param frameData
     */
    void onWebSocketPing(WebSocket conn, FrameData frameData);

    /**
     * 发送ping frame之前，自定义ping frame
     * @param conn
     * @return
     */
    PingFrame onPreparePing(WebSocket conn);

    /**
     * 接收到 Pong 帧时调用，确认连接的活跃性和延迟。
     *
     * @param conn
     * @param frameData
     */
    void onWebSocketPong(WebSocket conn, FrameData frameData);

    /**
     * 当需要向 WebSocket连接写入数据时调用此方法，由数据发送线程发起的。
     * 这个方法可以通知选择器线程（selector thread）有数据需要写入到套接字中
     *
     * @param conn
     */
    void onWriteDemand(WebSocket conn);

    /**
     * 返回WebSocket服务端实例地址信息
     *
     * @param conn
     * @return
     */
    InetSocketAddress getLocalSocketAddress(WebSocket conn);

    /**
     * 返回WebSocket实例连接的客户端地址信息
     *
     * @param conn
     * @return
     */
    InetSocketAddress getRemoteSocketAddress(WebSocket conn);
}
