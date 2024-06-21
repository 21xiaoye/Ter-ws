package org.ter_ws.framing;

import org.ter_ws.enums.OpCode;

public class PongFrame extends ControlFrame{
    public PongFrame() {
        super(OpCode.PONG);
    }
    public PongFrame(PingFrame pingFrame) {
        super(OpCode.PONG);
        setUnMaskedPayload(pingFrame.getPayloadData());
    }
}
