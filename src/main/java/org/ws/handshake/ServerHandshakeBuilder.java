package org.ws.handshake;

public interface ServerHandshakeBuilder extends HandshakeBuild, ServerHandshake{
    void setHttpStatus(short httpStatus);
    void setHttpStatusMessage(String httpStatusMessage);
}
