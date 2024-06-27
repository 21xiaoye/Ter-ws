package org.ws.draft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ws.WebSocketImpl;
import org.ws.enums.HandshakeState;
import org.ws.exceptions.InvalidDataException;
import org.ws.exceptions.InvalidHandshakeException;
import org.ws.framing.FrameData;
import org.ws.handshake.ClientHandshake;
import org.ws.handshake.ServerHandshake;
import org.ws.protocols.IProtocol;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * 封装WebSocket相关协议的实现
 */
public class Draft_6455 extends Draft{
    /**
     * 随机字符串，用于安全校验和协议升级字段
     */
    private static final String SEC_WEB_SOCKET_KEY = "Sec-WebSocket-Key";
    /**
     * 协议握手字段，可以指定一个协议或者多个协议，服务器选择接受其中一个子协议进行通信
     */
    private static final String SEC_WEB_SOCKET_PROTOCOL = "Sec-WebSocket-Protocol";
    /**
     * 扩展的握手字段
     */
    private static final String SEC_WEB_SOCKET_EXTENSIONS = "Sec-WebSocket-Extensions";
    /**
     * 接受的握手字段，服务器响应时在响应标头中出现，只会出现一次
     */
    private static final String SEC_WEB_SOCKET_ACCEPT = "Sec-WebSocket-Accept";
    /**
     * 升级请求，指定为websocket，表示升级为WebSocket协议
     */
    private static final String UPGRADE = "Upgrade";
    /**
     * 握手连接的特定字段
     */
    private static final String CONNECTION = "Connection";
    private final Logger logger = LoggerFactory.getLogger(Draft_6455.class);
    /**
     * 此草案中使用的协议属性
     */
    private IProtocol protocol;
    /**
     * 所有可用协议属性
     */
    private List<IProtocol> knowProtocols;
    /**
     * 当前帧的属性
     */
    private FrameData currentContinuousFrame;
    /**
     * 当前帧的有效载荷
     */
    private final List<ByteBuffer> byteBufferList;
    /**
     * 不完整帧的属性
     */
    private ByteBuffer incompleteFrame;
    /**
     * 可重用随机实例的属性
     */
    private final SecureRandom reusableRandom = new SecureRandom();

    public Draft_6455(){
        byteBufferList = new ArrayList<>();
    }
    @Override
    public Draft copyInstance() {
        ArrayList<Object> newExtensions = new ArrayList<>();
        return new Draft_6455();
    }

    @Override
    public void reset() {

    }

    @Override
    public List<FrameData> translateFrame(ByteBuffer buffer) throws InvalidDataException {
        return null;
    }

    @Override
    public void processFrame(WebSocketImpl webSocketImpl, FrameData frame) throws InvalidDataException {

    }

    @Override
    public HandshakeState acceptHandshakeAsServer(ClientHandshake clientHandshake) throws InvalidHandshakeException {
        return null;
    }

    @Override
    public HandshakeState acceptHandshakeAsClient(ClientHandshake request, ServerHandshake response) throws InvalidHandshakeException {
        return null;
    }
}
