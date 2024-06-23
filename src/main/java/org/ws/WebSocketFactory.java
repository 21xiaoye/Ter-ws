package org.ws;

import org.ws.draft.Draft;

import java.util.List;

public interface WebSocketFactory {
    WebSocket createdWebSocket(WebSocketAdapter webSocketAdapter, Draft draft);
    WebSocket createdWebSocket(WebSocketAdapter webSocketAdapter, List<Draft> draftList);
}
