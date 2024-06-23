package org.ws;

import org.ws.draft.Draft;

import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;

public interface WebSocketServerFactory extends WebSocketFactory{
    @Override
    WebSocketImpl createdWebSocket(WebSocketAdapter webSocketAdapter, Draft draft);
    @Override
    WebSocketImpl createdWebSocket(WebSocketAdapter webSocketAdapter, List<Draft> draftList);
}
