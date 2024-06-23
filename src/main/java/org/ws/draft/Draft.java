package org.ws.draft;

import org.ws.enums.OpCode;
import org.ws.enums.Role;

/**
 * WebSocket 6455协议的相关实
 */
public abstract class Draft{
    protected Role role = null;
    protected OpCode continuousFrameType = null;
    public abstract Draft copyInstance();
}
