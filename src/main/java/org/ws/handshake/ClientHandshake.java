package org.ws.handshake;

public interface ClientHandshake extends HandshakeData{
    /**
     * ����http�����URL
     * @return
     */
    String getResourceDescriptor();
}
