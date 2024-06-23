package org.ws.exceptions;

import org.ws.framing.CloseFrame;

/**
 * 无效帧异常处理类
 */
public class InvalidFrameException extends InvalidDataException{
    private static final long serialVersionUID = -9016496369828887591L;
    public InvalidFrameException() {
        super(CloseFrame.PROTOCOL_ERROR);
    }
    public InvalidFrameException(String errorMessage) {
        super(CloseFrame.PROTOCOL_ERROR, errorMessage);
    }
    public InvalidFrameException(Throwable t) {
        super(CloseFrame.PROTOCOL_ERROR, t);
    }
    public InvalidFrameException(String s, Throwable t) {
        super(CloseFrame.PROTOCOL_ERROR, s, t);
    }
}
