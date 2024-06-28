package org.ws.draft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ws.WebSocketImpl;
import org.ws.enums.HandshakeState;
import org.ws.exceptions.InvalidDataException;
import org.ws.exceptions.InvalidHandshakeException;
import org.ws.framing.FrameData;
import org.ws.handshake.ClientHandshake;
import org.ws.handshake.HandshakeBuild;
import org.ws.handshake.ServerHandshake;
import org.ws.handshake.ServerHandshakeBuilder;
import org.ws.protocols.IProtocol;
import org.ws.protocols.Protocol;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;

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
        this(Collections.<IProtocol>singletonList(new Protocol("")));
    }
    public Draft_6455(List<IProtocol> inputProtocols){
        if(Objects.isNull(inputProtocols)){
            throw new IllegalArgumentException();
        }
        knowProtocols = new ArrayList<>(inputProtocols.size());
        byteBufferList =new ArrayList<>();
        knowProtocols.addAll(inputProtocols);
        currentContinuousFrame = null;
    }
    @Override
    public Draft copyInstance() {
        ArrayList<IProtocol> newProtocols = new ArrayList<>();
        for (IProtocol knownProtocol : getKnowProtocols()){
            newProtocols.add(knownProtocol);
        }
        return new Draft_6455(newProtocols);
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
        int v = readVersion(clientHandshake);
        if(v != 13){
            logger.trace("acceptHandshakeAsServer - Wrong websocket version!");
            return HandshakeState.NOT_MATCHED;
        }
        return containsRequestedProtocol(SEC_WEB_SOCKET_PROTOCOL);
    }

    @Override
    public HandshakeState acceptHandshakeAsClient(ClientHandshake request, ServerHandshake response) throws InvalidHandshakeException {
        return null;
    }

    @Override
    public HandshakeBuild postProcessHandshakeResponseAsServer(ClientHandshake request, ServerHandshakeBuilder response) throws InvalidHandshakeException {
        response.put(UPGRADE, "websocket");
        response.put(CONNECTION, request.getFieldValue(CONNECTION));
        String secKey = request.getFieldValue(SEC_WEB_SOCKET_KEY);
        if(Objects.isNull(secKey) || "".equals(secKey)){
            throw new InvalidHandshakeException("missing Sec-WebSocket-Key");
        }
        response.put(SEC_WEB_SOCKET_ACCEPT, generateFinalKey(secKey));
        if(Objects.nonNull(getProtocol())  && getProtocol().getProvidedProtocol().length()!=0){
            response.put(SEC_WEB_SOCKET_PROTOCOL, getProtocol().getProvidedProtocol());
        }
        response.setHttpStatusMessage("Web Socket Protocol Handshake");
        response.put("Server", "TooTalNate Java-WebSocket");
        response.put("Date", getServerTime());
        return response;

    }

    public IProtocol getProtocol() {
        return protocol;
    }

    public List<IProtocol> getKnowProtocols() {
        return knowProtocols;
    }

    private HandshakeState containsRequestedProtocol(String requestedProtocol){
        for (IProtocol knownProtocol : knowProtocols){
            if(knownProtocol.acceptProvidedProtocol(requestedProtocol)){
                protocol = knownProtocol;
                logger.trace("acceptHandshake - Matching protocol found: {}", protocol);
                return HandshakeState.MATCHED;
            }
        }
        return HandshakeState.NOT_MATCHED;
    }

    private String generateFinalKey(String in) {
        String seckey = in.trim();
        String acc = seckey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        MessageDigest sh1;
        try {
            sh1 = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
        return org.java_websocket.util.Base64.encodeBytes(sh1.digest(acc.getBytes()));
    }

    private String getServerTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }






























}
