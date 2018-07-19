package net.sourcedestination.codecafe;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.*;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.logging.Logger;

@Configuration
@EnableWebSocket
public class WebSocketsConfiguration implements WebSocketConfigurer {
    private final Logger logger = Logger.getLogger(WebSocketsConfiguration.class.getCanonicalName());

    @Autowired private LessonController lessons;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(
                new LessonWebsocketHandler(lessons, "history",
                        (tool, session) -> tool.attachHistoryListener(
                                (snippetEvent) -> {
                                    try {
                                        var msg = "input: " + snippetEvent.snippet().source() + "\n\t\tresult --> " +
                                                snippetEvent.value() + "\n";
                                        session.sendMessage(new TextMessage(msg)); // TODO: encode to json
                                    } catch (Exception e) {
                                        // TODO: log error
                                    }
                                }
                        )
                ),"/lessons/**/history");

        registry.addHandler(
                new LessonWebsocketHandler(lessons, "variable listener",
                        (tool, session) -> tool.attachVariableListener(
                                (varMap) -> {
                                    try {
                                        // TODO: format in JSON
                                        // TODO: only send updated vars
                                        StringBuilder sb = new StringBuilder();
                                        varMap.forEach((var, value) ->
                                                sb.append(var.typeName() + " " + var.name() +
                                                        " = " + value + "\n"));
                                        session.sendMessage(new TextMessage(sb.toString()));
                                    } catch (Exception e) {
                                        // TODO: log error
                                    }
                                }
                        )
                ),"/lessons/**/variables");

        registry.addHandler(
                new LessonWebsocketHandler(lessons, "error listener",
                        (tool, session) -> tool.attachErrorListener(
                                (code, error) -> {
                                    try {
                                        // TODO: format in JSON
                                        session.sendMessage(new TextMessage("ERROR executing " + code
                                                + ": " + error + "\n"));
                                    } catch (Exception e) {
                                        // TODO: log error
                                    }
                                }
                        )
                ),"/lessons/**/errors");

        registry.addHandler(
                new LessonWebsocketHandler(lessons, "method listener",
                        (tool, session) -> tool.attachMethodListener(
                                (methodSnippets) -> {
                                    // TODO: send method definitions
                                    // TODO: only send methods that changed
                                }
                        )
                ),"/lessons/**/methods");

        registry.addHandler(
                new LessonWebsocketHandler(lessons, "stdout listener",
                        (tool, session) -> tool.attachStdoutListener(
                                (msg) -> {
                                    // TODO: send message
                                }
                        )
                ),"/lessons/**/stdout");
    }
}
