package org.ws.framing;

import org.ws.enums.OpCode;

/**
 * ������֡
 */
public class BinaryFrame extends DataFrame{
    public BinaryFrame(){
        super(OpCode.BINARY);
    }
}
