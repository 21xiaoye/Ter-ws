package org.ws.draft;

import org.ws.WebSocketImpl;
import org.ws.enums.HandshakeState;
import org.ws.exceptions.InvalidDataException;
import org.ws.exceptions.InvalidHandshakeException;
import org.ws.framing.FrameData;
import org.ws.handshake.ClientHandshake;
import org.ws.handshake.ServerHandshake;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * 封装WebSocket相关协议的实现
 */
public class Draft_6455 extends Draft{
    @Override
    public Draft copyInstance() {
        return null;
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
