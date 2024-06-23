package org.ws.handshake;

/**
 * 客户端握手实现
 */
public class HandshakeImplClient extends HandshakeImpl implements ClientHandshakeBuild{
    /**
     * 服务端资源路径，默认所有资源
     */
    private String resourceDescriptor = "*";
    @Override
    public String getResourceDescriptor() {
        return resourceDescriptor;
    }

    @Override
    public void setResourceDescriptor(String resourceDescriptor) {
        if(resourceDescriptor == null){
            throw new IllegalArgumentException("http resource descriptor must not be null");
        }
        this.resourceDescriptor = resourceDescriptor;
    }
}
