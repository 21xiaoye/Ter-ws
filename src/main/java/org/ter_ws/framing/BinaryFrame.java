package org.ter_ws.framing;

import org.ter_ws.enums.OpCode;

/**
 * 二进制帧
 */
public class BinaryFrame extends DataFrame{
    public BinaryFrame(){
        super(OpCode.BINARY);
    }
}
