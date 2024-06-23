package org.ws.protocols;

public interface IProtocol {
    /**
     * Sec-WebSocket-Protocol�ֶ��Ƿ����Э��
     *
     * @param inputProtocolHeader
     * @return
     */
    boolean acceptProvidedProtocol(String inputProtocolHeader);
    String getProvidedProtocol();
    IProtocol copyInstance();
    String toString();
}
