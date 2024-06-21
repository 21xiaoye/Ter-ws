package org.ter_ws.framing;

import org.ter_ws.enums.OpCode;

public class ContinuousFrame extends DataFrame{
    public ContinuousFrame(){
        super(OpCode.CONTINUOUS);
    }
}
