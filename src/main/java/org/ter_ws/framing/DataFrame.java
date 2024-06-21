package org.ter_ws.framing;

import org.ter_ws.enums.OpCode;
import org.ter_ws.exceptions.InvalidDataException;

/**
 * 数据帧抽象类
 */
public abstract class DataFrame extends FrameDataImpl{
    public DataFrame(OpCode opCode) {
        super(opCode);
    }

    @Override
    public void isValid() throws InvalidDataException {

    }
}
