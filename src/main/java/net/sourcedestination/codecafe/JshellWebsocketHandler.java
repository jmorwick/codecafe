package net.sourcedestination.codecafe;

import jdk.jshell.VarSnippet;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;

@Component
public class JshellWebsocketHandler implements WebSocketHandler {

    Logger logger = Logger.getLogger(JshellWebsocketHandler.class.getCanonicalName());

    Map<String,JShellEvalTerminal> jshellTerms = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {

        var path = session.getUri().getPath();
        var lessonId = path.substring(path.lastIndexOf('/')+1);
        logger.info("Connection " + session.getId() + " to lesson " + lessonId);
        jshellTerms.put(session.getId(), new JShellEvalTerminal(
                msg -> {
                    try {
                        session.sendMessage(new TextMessage(msg));
                    } catch (Exception e) {
                        logger.info("Error sending message: " + e.getMessage());
                    }
                }
        ));

        session.sendMessage(new TextMessage(session.getId())); // handshake message
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
        jshellTerms.get(session.getId()).stop();
        logger.info("Session #" + session.getId() + " closed");

        // TODO: maintain session between reloads for logged in users.
        // TODO: Only close sessions on logouts / save progress and reload when user logs back in
    }


    public void attachVariableListener(String sessionId, Consumer<Map<VarSnippet, String>> callback) {
        logger.info(" listening to vars on session: " + sessionId);
        logger.info(jshellTerms.keySet().toString());
        jshellTerms.get(sessionId).attachVariableListener(callback);
    }

    @Override
    public boolean supportsPartialMessages() { return false; }
}
