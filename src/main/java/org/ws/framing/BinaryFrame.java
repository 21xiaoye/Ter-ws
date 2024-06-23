package org.ws.framing;

import org.ws.enums.OpCode;

/**
 * ถþฝ๘ึฦึก
 */
public class BinaryFrame extends DataFrame{
    public BinaryFrame(){
        super(OpCode.BINARY);
    }
}
