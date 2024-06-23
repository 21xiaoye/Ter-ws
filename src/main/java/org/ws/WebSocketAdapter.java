package org.ws;

import org.ws.draft.Draft;
import org.ws.exceptions.InvalidDataException;
import org.ws.framing.FrameData;
import org.ws.framing.PingFrame;
import org.ws.framing.PongFrame;
import org.ws.handshake.ClientHandshake;
import org.ws.handshake.HandshakeImplServer;
import org.ws.handshake.ServerHandshake;
import org.ws.handshake.ServerHandshakeBuilder;

import java.util.Objects;

public abstract class WebSocketAdapter implements WebSocketListener{
    private PingFrame pingFrame;

    @Override
    public ServerHandshakeBuilder onWebSocketHandshakeReceivedAsServer(WebSocket conn, Draft draft, ClientHandshake request) throws InvalidDataException {
        return new HandshakeImplServer();
    }

    @Override
    public void onWebsocketHandshakeSentAsClient(WebSocket conn, ClientHandshake request) throws InvalidDataException {

    }

    @Override
    public void onWebsocketHandshakeReceivedAsClient(WebSocket conn, ClientHandshake request, ServerHandshake response) throws InvalidDataException {

    }

    @Override
    public void onWebSocketPong(WebSocket conn, FrameData frameData) {

    }

    @Override
    public void onWebSocketPing(WebSocket conn, FrameData frameData) {
        conn.sendFrame(new PongFrame((PingFrame) frameData));
    }

    @Override
    public PingFrame onPreparePing(WebSocket conn) {
        if(Objects.isNull(pingFrame)){
            pingFrame = new PingFrame();
        }
        return pingFrame;
    }
}
