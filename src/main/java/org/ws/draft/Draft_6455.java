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
 * ��װWebSocket���Э���ʵ��
 */
public class Draft_6455 extends Draft{
    /**
     * ����ַ��������ڰ�ȫУ���Э�������ֶ�
     */
    private static final String SEC_WEB_SOCKET_KEY = "Sec-WebSocket-Key";
    /**
     * Э�������ֶΣ�����ָ��һ��Э����߶��Э�飬������ѡ���������һ����Э�����ͨ��
     */
    private static final String SEC_WEB_SOCKET_PROTOCOL = "Sec-WebSocket-Protocol";
    /**
     * ��չ�������ֶ�
     */
    private static final String SEC_WEB_SOCKET_EXTENSIONS = "Sec-WebSocket-Extensions";
    /**
     * ���ܵ������ֶΣ���������Ӧʱ����Ӧ��ͷ�г��֣�ֻ�����һ��
     */
    private static final String SEC_WEB_SOCKET_ACCEPT = "Sec-WebSocket-Accept";
    /**
     * ��������ָ��Ϊwebsocket����ʾ����ΪWebSocketЭ��
     */
    private static final String UPGRADE = "Upgrade";
    /**
     * �������ӵ��ض��ֶ�
     */
    private static final String CONNECTION = "Connection";
    private final Logger logger = LoggerFactory.getLogger(Draft_6455.class);
    /**
     * �˲ݰ���ʹ�õ�Э������
     */
    private IProtocol protocol;
    /**
     * ���п���Э������
     */
    private List<IProtocol> knowProtocols;
    /**
     * ��ǰ֡������
     */
    private FrameData currentContinuousFrame;
    /**
     * ��ǰ֡����Ч�غ�
     */
    private final List<ByteBuffer> byteBufferList;
    /**
     * ������֡������
     */
    private ByteBuffer incompleteFrame;
    /**
     * ���������ʵ��������
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
