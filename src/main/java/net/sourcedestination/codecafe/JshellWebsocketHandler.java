package net.sourcedestination.codecafe;

import jdk.jshell.VarSnippet;
import net.sourcedestination.funcles.tuple.Tuple2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.socket.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static net.sourcedestination.funcles.tuple.Tuple.makeTuple;

@Controller
public class JshellWebsocketHandler implements WebSocketHandler {

    Logger logger = Logger.getLogger(JshellWebsocketHandler.class.getCanonicalName());

    Map<Tuple2<String,String>,JShellEvalTerminal> jshellTerms = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        var path = session.getUri().getPath();
        var lessonId = path.substring(path.lastIndexOf('/')+1);
        logger.info("Connection " + session.getId() + " to lesson " + lessonId);
        var id = makeTuple(lessonId,session.getPrincipal().getName());
        Consumer<String> pipeToJs = msg -> {
            try {
                session.sendMessage(new TextMessage(msg));
            } catch (Exception e) {
                logger.info("Error sending message: " + e.getMessage());
            }
        };

        if(!jshellTerms.containsKey(id))
            jshellTerms.put(id, new JShellEvalTerminal(pipeToJs));
        else
            jshellTerms.get(id).updateConsumer(pipeToJs);


        // TODO: resend history? Use separate socket for history?
        session.sendMessage(new TextMessage("> "));
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws IOException {
        var path = session.getUri().getPath();
        var lessonId = path.substring(path.lastIndexOf('/')+1);
        if(jshellTerms.containsKey(makeTuple(lessonId,session.getPrincipal().getName()))) {
            jshellTerms.get(makeTuple(lessonId,session.getPrincipal().getName()))
                    .receiveMessage(message.getPayload().toString());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable error) {

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        var path = session.getUri().getPath();
        var lessonId = path.substring(path.lastIndexOf('/')+1);

        // TODO: maintain session between reloads for logged in users.
        // TODO: close sessions on logouts / save progress and reload when user logs back in
    }

    public void attachVariableListener(WebSocketSession session, Consumer<Map<VarSnippet, String>> callback) {
        var path = session.getUri().getPath();
        var lessonId = path.substring(path.lastIndexOf('/')+1);
        logger.info(jshellTerms.keySet().toString());
        jshellTerms.get(makeTuple(lessonId,session.getPrincipal().getName()))
                .attachVariableListener(callback);
    }

    @Override
    public boolean supportsPartialMessages() { return false; }

    @PostMapping("/exec/{lesson}")
    public void executeSnippet(@PathVariable("lesson") String lesson,
                                       @RequestParam("code") String code,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
        var name = request.getUserPrincipal().getName();
        var id = makeTuple(lesson, name);
        logger.info("User " + name + " on lesson " + lesson + " sent " + code);
        jshellTerms.get(id).receiveMessage(code);
    }
}
