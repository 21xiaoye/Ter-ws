package org.ws.framing;

import org.ws.enums.OpCode;
import org.ws.exceptions.InvalidDataException;

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
