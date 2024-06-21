package org.ter_ws.drafts;

import org.ter_ws.enums.OpCode;
import org.ter_ws.enums.Role;

/**
 * WebSocket 6455协议的相关实现
 */
public abstract class Draft{
    protected Role role = null;
    protected OpCode continuousFrameType = null;
}
