package org.ws.framing;

import org.ws.enums.OpCode;

public class ContinuousFrame extends DataFrame{
    public ContinuousFrame(){
        super(OpCode.CONTINUOUS);
    }
}
