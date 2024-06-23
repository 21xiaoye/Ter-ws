package org.ws.protocols;

public interface IProtocol {
    /**
     * Sec-WebSocket-Protocol字段是否包含协议
     *
     * @param inputProtocolHeader
     * @return
     */
    boolean acceptProvidedProtocol(String inputProtocolHeader);
    String getProvidedProtocol();
    IProtocol copyInstance();
    String toString();
}
