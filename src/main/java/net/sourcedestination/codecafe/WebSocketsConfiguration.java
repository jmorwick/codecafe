package net.sourcedestination.codecafe;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.*;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Configuration
@EnableWebSocket
public class WebSocketsConfiguration implements WebSocketConfigurer {
    private final Logger logger = Logger.getLogger(WebSocketsConfiguration.class.getCanonicalName());

    @Autowired private ExerciseController exercises;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(
                new ExerciseWebsocketHandler(exercises, "history",
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
                ),"/exercises/**/history");

        registry.addHandler(
                new ExerciseWebsocketHandler(exercises, "variable listener",
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
                ),"/exercises/**/variables");

        registry.addHandler(
                new ExerciseWebsocketHandler(exercises, "error listener",
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
                ),"/exercises/**/errors");

        registry.addHandler(
                new ExerciseWebsocketHandler(exercises, "method listener",
                        (tool, session) -> tool.attachMethodListener(
                                (methodSnippets) -> {
                                    GsonBuilder builder = new GsonBuilder();
                                    Gson gson = builder.create();
                                    try {
                                        session.sendMessage(new TextMessage(
                                                gson.toJson(methodSnippets.stream()
                                                        .map(ms -> List.of(ms.name(), ms.signature(), ms.source()))
                                                        .collect(Collectors.toList()))));
                                    } catch(IOException e) {
                                        // TODO: log error
                                    }
                                    // TODO: only send methods that changed
                                }
                        )
                ),"/exercises/**/methods");

        registry.addHandler(
                new ExerciseWebsocketHandler(exercises, "stdout listener",
                        (tool, session) -> tool.attachStdoutListener(
                                (msg) -> {
                                    try {
                                        session.sendMessage(new TextMessage(msg));
                                    } catch(IOException e) {
                                        // TODO: log error
                                    }
                                }
                        )
                ),"/exercises/**/stdout");
    }
}
