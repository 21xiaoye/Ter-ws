package org.ws.draft;

import org.ws.WebSocketImpl;
import org.ws.enums.HandshakeState;
import org.ws.enums.OpCode;
import org.ws.enums.Role;
import org.ws.exceptions.InvalidDataException;
import org.ws.exceptions.InvalidHandshakeException;
import org.ws.framing.FrameData;
import org.ws.handshake.ClientHandshake;
import org.ws.handshake.HandshakeData;
import org.ws.handshake.ServerHandshake;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * WebSocket 6455协议的相关实
 */
public abstract class Draft{
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
        return null;
    }
    public void setRole(Role role) {
        this.role = role;
    }

}
