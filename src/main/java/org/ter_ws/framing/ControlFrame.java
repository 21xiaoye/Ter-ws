package org.ter_ws.framing;

import org.ter_ws.enums.OpCode;
import org.ter_ws.exceptions.InvalidDataException;
import org.ter_ws.exceptions.InvalidFrameException;

/**
 * 控制帧抽象类
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
