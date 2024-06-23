package org.ws.framing;

import org.ws.enums.OpCode;

public class CloseFrame extends ControlFrame{
    /**
     * �������ӹر�
     */
    public static final int NORMAL = 1000;
    /**
     * �ͻ������ӹر�
     */
    public static final int GOING_AWAY = 1001;
    /**
     * �ͻ���Э����������ӹر�
     */
    public static final int PROTOCOL_ERROR = 1002;
    public static final int ABNORMAL_CLOSE = 1006;

    public CloseFrame(OpCode opCode) {
        super(opCode);
    }
}
