package org.ws.enums;


/**
 * 内容类型枚举
 */
public enum OpCode {
    CONTINUOUS((byte) 0), TEXT((byte) 1), BINARY((byte) 2), PING((byte) 3), PONG((byte) 4), CLOSING((byte) 5);
    private final byte code;

    public byte getCode() {
        return code;
    }
    OpCode(byte code){
        this.code = code;
    }
}
