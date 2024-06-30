package org.ws.draft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ws.WebSocketImpl;
import org.ws.enums.HandshakeState;
import org.ws.enums.Role;
import org.ws.exceptions.InvalidDataException;
import org.ws.exceptions.InvalidHandshakeException;
import org.ws.exceptions.NotSendAbleException;
import org.ws.framing.FrameData;
import org.ws.framing.TextFrame;
import org.ws.handshake.ClientHandshake;
import org.ws.handshake.HandshakeBuild;
import org.ws.handshake.ServerHandshake;
import org.ws.handshake.ServerHandshakeBuilder;
import org.ws.protocols.IProtocol;
import org.ws.protocols.Protocol;
import org.ws.utils.CharsetFunctions;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;

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
    public ByteBuffer createBinaryFrame(FrameData frameData) {
        return createByteBufferFromFrameData(frameData);
    }

    private ByteBuffer createByteBufferFromFrameData(FrameData frameData){
        ByteBuffer payloadData = frameData.getPayloadData();
        boolean mask = role == Role.CLIENT;
        int sizeBytes = getSizeBytes(payloadData);
        ByteBuffer buffer = ByteBuffer.allocate(1 + (sizeBytes > 1 ? sizeBytes + 1 : sizeBytes) + (mask ? 4 : 0) + payloadData.remaining());
        byte one = (byte) (frameData.isFin() ? -128 : 0);
         one |= frameData.getOpcode().getCode();
         if(frameData.isRSV1()){
             one |= getRSVByte(1);
         }
         if(frameData.isRSV2()){
             one |= getRSVByte(2);
         }
         if(frameData.isRSV3()){
             one |= getRSVByte(3);
         }

         buffer.put(one);
        byte[] payloadLengthBytes = toByteArray(payloadData.remaining(), sizeBytes);
        assert (payloadLengthBytes.length == sizeBytes);
        if(sizeBytes == 1){
            buffer.put((byte) (payloadLengthBytes[0] | getMaskByte(mask)));
        } else if (sizeBytes == 2) {
            buffer.put((byte) ((byte) 126 | getMaskByte(mask)));
            buffer.put(payloadLengthBytes);
        }else if(sizeBytes == 8){
            buffer.put((byte) ((byte)127 | getMaskByte(mask)));
            buffer.put(payloadLengthBytes);
        }else{
            throw new IllegalStateException("Size representation not supported/specified");
        }
        if(mask){
            ByteBuffer maskKey = ByteBuffer.allocate(4);
            maskKey.putInt(reusableRandom.nextInt());
            buffer.put(maskKey.array());
            for (int i =0; payloadData.hasRemaining();i++){
                buffer.put((byte) (payloadData.get( i % 4)));
            }
        }else {
            buffer.put(payloadData);
            payloadData.flip();
        }
        assert (buffer.remaining() == 0) : buffer.remaining();
        buffer.flip();
        return buffer;
    }
    private int getSizeBytes(ByteBuffer buffer){
        if(buffer.remaining() <= 125) {
            return 1;
        }
        if(buffer.remaining() < 65535){
            return 2;
        }
        return 0;
    }
    private byte getRSVByte(int rsv) {
        switch (rsv) {
            case 1 : // 0100 0000
                return 0x40;
            case 2 : // 0010 0000
                return 0x20;
            case 3 : // 0001 0000
                return 0x10;
            default:
                return 0;
        }
    }
    private byte[] toByteArray(long val, int byteCount) {
        byte[] buffer = new byte[byteCount];
        int highest = 8 * byteCount - 8;
        for (int i = 0; i < byteCount; i++) {
            buffer[i] = (byte) (val >>> (highest - 8 * i));
        }
        return buffer;
    }
    private byte getMaskByte(boolean mask) {
        return mask ? (byte) -128 : 0;
    }
    @Override
    public List<FrameData> createFrame(String text, boolean mask) {
        TextFrame textFrame = new TextFrame();
        textFrame.setUnMaskedPayload(ByteBuffer.wrap(CharsetFunctions.utf8Bytes(text)));
        textFrame.setTransferMasked(mask);
        try {
            textFrame.isValid();
        }catch (InvalidDataException exception){
            throw new NotSendAbleException(exception);
        }
        return Collections.singletonList(textFrame);
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
