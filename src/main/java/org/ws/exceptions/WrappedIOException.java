package org.ws.exceptions;

import org.ws.WebSocket;

import java.io.IOException;


public class WrappedIOException extends Exception{
    private final transient WebSocket webSocket;
    private final IOException ioException;

    public WrappedIOException(WebSocket webSocket, IOException ioException) {
        this.webSocket = webSocket;
        this.ioException = ioException;
    }

    public WebSocket getWebSocket() {
        return webSocket;
    }

    public IOException getIoException() {
        return ioException;
    }
}
