package org.ws.framing;

import org.ws.enums.OpCode;

public class PingFrame extends ControlFrame{
    public PingFrame() {
        super(OpCode.PING);
    }
}
