package org.ws.framing;

import org.ws.enums.OpCode;
import org.ws.exceptions.InvalidDataException;

/**
 * ����֡������
 */
public abstract class DataFrame extends FrameDataImpl{
    public DataFrame(OpCode opCode) {
        super(opCode);
    }

    @Override
    public void isValid() throws InvalidDataException {

    }
}
