package org.ter_ws.framing;

import org.ter_ws.enums.OpCode;

public class CloseFrame extends ControlFrame{
    /**
     * 正常连接关闭
     */
    public static final int NORMAL = 1000;
    /**
     * 客户端连接关闭
     */
    public static final int GOING_AWAY = 1001;
    /**
     * 客户端协议错误导致连接关闭
     */
    public static final int PROTOCOL_ERROR = 1002;

    public CloseFrame(OpCode opCode) {
        super(opCode);
    }
}
