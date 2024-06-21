package org.ter_ws.handshake;

public interface ClientHandshake extends HandshakeData{
    /**
     * 返回http请求的URL
     * @return
     */
    String getResourceDescriptor();
}
