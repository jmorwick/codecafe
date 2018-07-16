package net.sourcedestination.codecafe;

import org.springframework.web.socket.*;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class JshellWebsocketHandler implements WebSocketHandler {

    Logger logger = Logger.getLogger(JshellWebsocketHandler.class.getCanonicalName());

    Map<String,JShellEvalTerminal> jshellTerms = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        jshellTerms.put(session.getId(), new JShellEvalTerminal(
                msg -> {
                    try {
                        session.sendMessage(new TextMessage(msg));
                    } catch (Exception e) {
                        logger.info("Error sending message: " + e.getMessage());
                    }
                }
        ));

    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws IOException {
        if(jshellTerms.containsKey(session.getId())) {
            jshellTerms.get(session.getId()).receiveMessage(message.getPayload().toString());
        }
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
