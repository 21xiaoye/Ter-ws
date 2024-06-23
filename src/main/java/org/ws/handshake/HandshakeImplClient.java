package org.ws.handshake;

/**
 * �ͻ�������ʵ��
 */
public class HandshakeImplClient extends HandshakeImpl implements ClientHandshakeBuild{
    /**
     * �������Դ·����Ĭ��������Դ
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
