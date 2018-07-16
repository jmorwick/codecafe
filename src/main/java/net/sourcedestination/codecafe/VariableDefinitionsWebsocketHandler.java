package net.sourcedestination.codecafe;

import jdk.jshell.VarSnippet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Component
public class VariableDefinitionsWebsocketHandler implements WebSocketHandler {

    Logger logger = Logger.getLogger(VariableDefinitionsWebsocketHandler.class.getCanonicalName());

    @Autowired
    private JshellWebsocketHandler jshellHandler;

    Map<String,JShellEvalTerminal> jshellTerms = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        var path = session.getUri().getPath();
        var jshellSession = path.substring(path.lastIndexOf('/')+1);
        logger.info("Connection " + session.getId() + " to var definitions from session " + jshellSession);
        jshellHandler.attachVariableListener(jshellSession, vars -> {
            try {
                logger.info(session.getId() + ": sending vars: " + formatVarsList(vars));
                session.sendMessage(new TextMessage(formatVarsList(vars)));
            } catch (Exception e) {
                logger.info("ERROR: " + e);
            }
        });
    }

    private String formatVarsList(Map<VarSnippet,String> vars) {
        StringBuilder sb = new StringBuilder();
        vars.forEach((var, value) -> sb.append(var.typeName() + " " + var.name() + " = " + value + "\n"));
        return sb.toString();
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws IOException {
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
