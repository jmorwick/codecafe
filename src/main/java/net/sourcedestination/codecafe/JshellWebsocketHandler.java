package net.sourcedestination.codecafe;

import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.logging.Logger;

public class JshellWebsocketHandler implements WebSocketHandler {

    Logger logger = Logger.getLogger(JshellWebsocketHandler.class.getCanonicalName());

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        session.sendMessage(new TextMessage("HI\n> ")); // (TODO: remove testing code)
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws IOException {
        logger.info(message.getPayload().toString());
        session.sendMessage(new TextMessage("no. \n> ")); // welcome the user (TODO: remove testing code)
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable error) {

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {

    }

    @Override
    public boolean supportsPartialMessages() { return false; }
}
