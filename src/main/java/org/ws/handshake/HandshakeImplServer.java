package org.ws.handshake;

/**
 * 服务端握手具体实现
 */
public class HandshakeImplServer extends HandshakeImpl implements ServerHandshakeBuilder{
    private short httpStatus;
    private String httpStatusMessage;
    @Override
    public short getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getHttpStatusMessage() {
        return httpStatusMessage;
    }

    @Override
    public void setHttpStatus(short httpStatus) {
        this.httpStatus = httpStatus;
    }

    @Override
    public void setHttpStatusMessage(String httpStatusMessage) {
        this.httpStatusMessage = httpStatusMessage;
    }
}
