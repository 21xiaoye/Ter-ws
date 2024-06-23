package org.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * �������˺Ϳͻ�������ʵ�ֵĻ���
 */
public abstract class AbstractWebSocket extends WebSocketAdapter{
    private final Logger logger = LoggerFactory.getLogger(AbstractWebSocket.class);
    private final Object syncConnectionLost = new Object();
    protected static int DEFAULT_READ_BUFFER_SIZE = 65536;
}
