package org.ws.handshake;

public interface HandshakeBuild extends HandshakeData{
    void setContent(byte[] content);
    void put(String name, String value);
}
