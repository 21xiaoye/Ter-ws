package org.ws.handshake;

public interface ServerHandshake extends HandshakeData{
    short getHttpStatus();
    String getHttpStatusMessage();
}
