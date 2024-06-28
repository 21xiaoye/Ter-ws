package org.ws.draft;

import org.ws.WebSocketImpl;
import org.ws.enums.HandshakeState;
import org.ws.enums.OpCode;
import org.ws.enums.Role;
import org.ws.exceptions.IncompleteHandshakeException;
import org.ws.exceptions.InvalidDataException;
import org.ws.exceptions.InvalidHandshakeException;
import org.ws.framing.FrameData;
import org.ws.handshake.*;
import org.ws.utils.CharsetFunctions;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * WebSocket 6455协议的相关实
 */
public abstract class Draft{
    private static final String SEC_WEB_SOCKET_VERSION ="Sec-WebSocket-Version";
    protected Role role = null;
    protected OpCode continuousFrameType = null;
    public abstract Draft copyInstance();
    public abstract void reset();
    public abstract List<FrameData> translateFrame(ByteBuffer buffer) throws InvalidDataException;
    public abstract void processFrame(WebSocketImpl webSocketImpl, FrameData frame)
            throws InvalidDataException;
    public abstract HandshakeState acceptHandshakeAsServer(ClientHandshake clientHandshake)
            throws InvalidHandshakeException;
    public abstract HandshakeState acceptHandshakeAsClient(ClientHandshake request,
                                                           ServerHandshake response) throws InvalidHandshakeException;
    public HandshakeData translateHandshake(ByteBuffer buf) throws InvalidHandshakeException {
        return translateHandshakeHttp(buf,null);
    }
    public abstract HandshakeBuild postProcessHandshakeResponseAsServer(ClientHandshake request,
                                                                          ServerHandshakeBuilder response) throws InvalidHandshakeException;

    public static HandshakeBuild translateHandshakeHttp(ByteBuffer buffer, Role role) throws InvalidHandshakeException {
        HandshakeBuild handshakeBuild;
        String line = readStringLine(buffer);
        if(Objects.isNull(line)){
            throw new IncompleteHandshakeException(buffer.capacity() + 128);
        }
        String[] strings = line.split(" ", 3);
        if(strings.length != 3){
            throw new InvalidHandshakeException();
        }
        if(role == Role.CLIENT){
            handshakeBuild = translateHandshakeHttpClient(strings, line);
        }else{
            handshakeBuild = translateHandshakeHttpServer(strings, line);
        }

        line = readStringLine(buffer);
        while (Objects.nonNull(line) && line.length() >0){
            String[] pair = line.split(":", 2);
            if(pair.length !=2){
                throw new InvalidHandshakeException("not an http header");
            }
            if(handshakeBuild.hasFieldValue(pair[0])){
        handshakeBuild.put(pair[0], handshakeBuild.getFieldValue(pair[0] +";"+pair[1].replaceFirst("^ +","")));
            }else {
                handshakeBuild.put(pair[0], pair[1].replaceFirst("^ +", ""));
            }
            line = readStringLine(buffer);
            if(Objects.isNull(line)){
                throw new IncompleteHandshakeException();
            }
        }
        return handshakeBuild;
    }

    public static String readStringLine(ByteBuffer byteBuffer){
        ByteBuffer buffer = readLine(byteBuffer);
        return Objects.isNull(buffer) ? null : CharsetFunctions.stringAscii(buffer.array(), 0, buffer.limit());
    }

    public static ByteBuffer readLine(ByteBuffer buffer){
        ByteBuffer allocate = ByteBuffer.allocate(buffer.remaining());
        byte prev;
        byte cur = '0';
        while (buffer.hasRemaining()){
            prev = cur;
            cur = buffer.get();
            allocate.put(cur);
            if(prev==(byte)'\r' && cur == (byte) '\n'){
                allocate.limit(allocate.position()-2);
                allocate.position(0);
                return allocate;
            }
        }
        buffer.position(buffer.position() - allocate.position());
        return null;
    }

    private static HandshakeBuild translateHandshakeHttpServer(String[] firstLineTokens, String line) throws InvalidHandshakeException{
        if(!"GET".equalsIgnoreCase(firstLineTokens[0])){
            throw new InvalidHandshakeException(String.format("Invalid request method received: %s Status line: %s",
                    firstLineTokens[0], line));
        }

        if (!"HTTP/1.1".equalsIgnoreCase(firstLineTokens[2])) {
            throw new InvalidHandshakeException(String
                    .format("Invalid status line received: %s Status line: %s", firstLineTokens[2], line));
        }
        HandshakeImplClient handshakeImplClient = new HandshakeImplClient();
        handshakeImplClient.setResourceDescriptor(firstLineTokens[1]);
        return handshakeImplClient;
    }

    private static HandshakeBuild translateHandshakeHttpClient(String[] firstLineTokens, String line) throws InvalidHandshakeException{
        if(!"101".equals(firstLineTokens[1])){
            throw new InvalidHandshakeException(String.format("Invalid status code received: %s Status line: %s",
                    firstLineTokens[1], line));
        }
        if(!"HTTP/1.1".equalsIgnoreCase(firstLineTokens[0])){
            throw new InvalidHandshakeException(String.format("Invalid status line received: %s Status line:%s", firstLineTokens[0], line));
        }

        HandshakeImplServer handshakeImplServer = new HandshakeImplServer();
        ServerHandshakeBuilder serverHandshakeBuilder = (ServerHandshakeBuilder)handshakeImplServer;
        serverHandshakeBuilder.setHttpStatus(Short.parseShort(firstLineTokens[1]));
        serverHandshakeBuilder.setHttpStatusMessage(firstLineTokens[2]);
        return handshakeImplServer;
    }

    public int readVersion(HandshakeData handshakeData){
        String fieldValue = handshakeData.getFieldValue(SEC_WEB_SOCKET_VERSION);
        if(fieldValue.length() > 0){
            int v;
            try {
                v = Integer.parseInt(fieldValue.trim());
                return v;
            }catch (NumberFormatException exception){
                return -1;
            }
        }
        return -1;
    }


    public List<ByteBuffer> createHandshake(HandshakeData handshakeData){
        return createHandshake(handshakeData, true);
    }

    public List<ByteBuffer> createHandshake(HandshakeData handshakeData, boolean withContent){
        StringBuilder builder = new StringBuilder(100);
        if(handshakeData instanceof ClientHandshake){
            builder.append("GET ").append(((ClientHandshake)handshakeData).getResourceDescriptor())
                    .append(" HTTP/1.1");
        }else if(handshakeData instanceof ServerHandshake){
            builder.append("HTTP/1.1 101 ").append(((ServerHandshake) handshakeData).getHttpStatusMessage());
        }else {
            throw new IllegalArgumentException("unknown role");
        }
        builder.append("\n");
        Iterator<String> stringIterator = handshakeData.iterateHttpFields();
        while (stringIterator.hasNext()){
            String fieldName = stringIterator.next();
            String fieldValue = handshakeData.getFieldValue(fieldName);
            builder.append(fieldName);
            builder.append(": ");
            builder.append(fieldValue);
            builder.append("\r\n");
        }
        builder.append("\r\n");
        byte[] bytes = CharsetFunctions.asciiBytes(builder.toString());
        byte[] content = withContent ? handshakeData.getContent() : null;
        ByteBuffer buffer = ByteBuffer.allocate((Objects.isNull(content) ? 0 : content.length) + bytes.length);
        buffer.put(bytes);
        if(Objects.nonNull(content)){
            buffer.put(content);
        }
        buffer.flip();
        return Collections.singletonList(buffer);
    }














    public void setRole(Role role) {
        this.role = role;
    }

}
