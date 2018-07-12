package net.sourcedestination.codecafe;

import jdk.jshell.tool.JavaShellToolBuilder;
import org.springframework.web.socket.*;

import java.io.*;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JshellWebsocketHandler implements WebSocketHandler {

    Logger logger = Logger.getLogger(JshellWebsocketHandler.class.getCanonicalName());

    Map<String,Thread> jshellThreads = new ConcurrentHashMap<>();
    Map<String,Thread> jshellReadingThreads = new ConcurrentHashMap<>();
    Map<String,PipedOutputStream> sendPipes = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        logger.info("openned session: " + session.getId());
        PipedInputStream inFromWs = new PipedInputStream();
        PipedOutputStream wsPrintsInToThis = new PipedOutputStream(inFromWs);
        PipedInputStream inFromJs = new PipedInputStream();
        PipedOutputStream jsPrintsInToThis = new PipedOutputStream(inFromJs);
        PrintStream ps = new PrintStream(jsPrintsInToThis);
        Thread jsrthread = new Thread(() -> {
            try {
                while (true) {
                    var c = inFromJs.read();
                    logger.info("message for " + session.getId()+": " + (char)c);
                    session.sendMessage(new TextMessage(""+((char)c)));
                }
            } catch(Exception e) { logger.log(Level.INFO, "error in jsrthread", e); }
        });
        Thread jsthread = new Thread(() -> {
            try {
                JavaShellToolBuilder.builder()
                        .in(inFromWs, inFromWs)
                        .err(ps)
                        .out(ps)
                        .promptCapture(false)
                        .run();
            } catch(Exception e) { logger.log(Level.INFO, "error in jsthread", e); }
        });
        jshellThreads.put(session.getId(), jsthread);
        jshellReadingThreads.put(session.getId(), jsrthread);
        sendPipes.put(session.getId(), wsPrintsInToThis);
        jsthread.start();
        jsrthread.start();
        wsPrintsInToThis.write("/help\n".getBytes());
    }

    private void writeToClient(String id, byte[] message) throws IOException {
        sendPipes.get(id).write(message);
        logger.info("message: " + Arrays.toString(message) +  " sent to jshell");
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws IOException {
        byte[] b = "a[27;9R\n\n".getBytes();
        b[0] = 27; // esc
        writeToClient(session.getId(), b);
        logger.info("incoming from web session " + session.getId()+": " + message.getPayload());
        writeToClient(session.getId(), (message.getPayload()+"\n").getBytes());
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
