package org.ws.server;

import org.ws.WebSocketAdapter;
import org.ws.WebSocketImpl;
import org.ws.WebSocketServerFactory;
import org.ws.draft.Draft;

import java.util.List;

public class DefaultWebSocketServerFactory implements WebSocketServerFactory {
    @Override
    public WebSocketImpl createdWebSocket(WebSocketAdapter webSocketAdapter, Draft draft) {
        return new WebSocketImpl(webSocketAdapter, draft);
    }

    @Override
    public WebSocketImpl createdWebSocket(WebSocketAdapter webSocketAdapter, List<Draft> draftList) {
        return new WebSocketImpl(webSocketAdapter, draftList);
    }
}
