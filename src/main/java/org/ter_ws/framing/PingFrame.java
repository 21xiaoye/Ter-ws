package org.ter_ws.framing;

import org.ter_ws.enums.OpCode;

public class PingFrame extends ControlFrame{
    public PingFrame() {
        super(OpCode.PING);
    }
}
