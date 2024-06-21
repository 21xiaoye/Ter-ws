package org.ter_ws.handshake;

public interface ClientHandshakeBuilder extends ClientHandshake{
    /**
     * 设置服务端资源路径
     */
    void setResourceDescriptor(String resourceDescriptor);
}
