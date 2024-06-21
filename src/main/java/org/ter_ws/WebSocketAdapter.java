package org.ter_ws;

import org.ter_ws.framing.FrameData;
import org.ter_ws.framing.PingFrame;
import org.ter_ws.framing.PongFrame;

import java.util.Objects;

public abstract class WebSocketAdapter implements WebSocketListener{
    private PingFrame pingFrame;

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
