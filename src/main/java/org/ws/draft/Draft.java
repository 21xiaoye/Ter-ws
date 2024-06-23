package org.ws.draft;

import org.ws.enums.OpCode;
import org.ws.enums.Role;

/**
 * WebSocket 6455Э������ʵ
 */
public abstract class Draft{
    protected Role role = null;
    protected OpCode continuousFrameType = null;
    public abstract Draft copyInstance();
}
