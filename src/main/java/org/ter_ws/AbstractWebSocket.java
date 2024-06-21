package org.ter_ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 处理服务端和客户端其它实现的基类
 */
public abstract class AbstractWebSocket extends WebSocketAdapter{
    private final Logger logger = LoggerFactory.getLogger(AbstractWebSocket.class);
    private final Object syncConnectionLost = new Object();
    protected static int DEFAULT_READ_BUFFER_SIZE = 65536;

























}
