package org.ws.handshake;

public interface ClientHandshakeBuild extends ClientHandshake{
    /**
     * 设置服务端资源路径
     */
    void setResourceDescriptor(String resourceDescriptor);
}
