package org.ter_ws.handshake;

public interface ServerHandshake extends HandshakeData{
    short getHttpStatus();
    String getHttpStatusMessage();
}
