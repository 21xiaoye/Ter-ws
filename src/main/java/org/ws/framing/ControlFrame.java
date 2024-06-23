package org.ws.framing;

import org.ws.enums.OpCode;
import org.ws.exceptions.InvalidDataException;
import org.ws.exceptions.InvalidFrameException;

/**
 * øÿ÷∆÷°≥ÈœÛ¿‡
 */
public abstract class ControlFrame extends FrameDataImpl{
    public ControlFrame(OpCode opCode){
        super(opCode);
    }

    @Override
    public void isValid() throws InvalidDataException {
        if (!isFin()) {
            throw new InvalidFrameException("Control frame can't have fin==false set");
        }
        if (isRSV1()) {
            throw new InvalidFrameException("Control frame can't have rsv1==true set");
        }
        if (isRSV2()) {
            throw new InvalidFrameException("Control frame can't have rsv2==true set");
        }
        if (isRSV3()) {
            throw new InvalidFrameException("Control frame can't have rsv3==true set");
        }
    }
}

