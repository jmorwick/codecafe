package net.sourcedestination.codecafe.networking;
import net.sourcedestination.codecafe.structure.ExerciseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import java.util.logging.Logger;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketsConfiguration extends AbstractSecurityWebSocketMessageBrokerConfigurer {
    private final Logger logger = Logger.getLogger(WebSocketsConfiguration.class.getCanonicalName());

    @Autowired private ExerciseController exercises;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/codecafe-websocket").withSockJS();
    }

    /*
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(
                new ExerciseWebsocketHandler(exercises, "history",
                        (tool, session) -> tool.attachHistoryListener(
                                (snippetEvent) -> {
                                    if(session.isOpen()) try {
                                        var msg = "input: " + snippetEvent.snippet().source() + "\n\t\tresult --> " +
                                                snippetEvent.value() + "\n";
                                        session.sendMessage(new TextMessage(msg)); // TODO: encode to json
                                    } catch (Exception e) {
                                        logger.log(Level.INFO, "error sending history", e);
                                    }
                                }
                        )
                ),"/exercises/** /history");

        registry.addHandler(
                new ExerciseWebsocketHandler(exercises, "variable listener",
                        (tool, session) -> tool.attachVariableListener(
                                (varMap) -> {
                                    if(session.isOpen()) try {
                                        // TODO: format in JSON
                                        // TODO: only send updated vars
                                        StringBuilder sb = new StringBuilder();
                                        varMap.forEach((var, value) ->
                                                sb.append(var.typeName() + " " + var.name() +
                                                        " = " + value + "\n"));
                                        session.sendMessage(new TextMessage(sb.toString()));
                                    } catch (Exception e) {
                                        logger.log(Level.INFO, "error sending variables", e);
                                    }
                                }
                        )
                ),"/exercises/** /variables");

        registry.addHandler(
                new ExerciseWebsocketHandler(exercises, "error listener",
                        (tool, session) -> tool.attachErrorListener(
                                (code, error) -> {
                                    if(session.isOpen()) try {
                                        // TODO: format in JSON
                                        session.sendMessage(new TextMessage("ERROR executing " + code
                                                + ": " + error + "\n"));
                                    } catch (Exception e) {
                                        logger.log(Level.INFO, "error sending error msg", e);
                                    }
                                }
                        )
                ),"/exercises/** /errors");

        registry.addHandler(
                new ExerciseWebsocketHandler(exercises, "method listener",
                        (tool, session) -> tool.attachMethodListener(
                                (methodSnippets) -> {
                                    if(session.isOpen()) try {
                                        GsonBuilder builder = new GsonBuilder();
                                        Gson gson = builder.create();
                                        session.sendMessage(new TextMessage(
                                                gson.toJson(methodSnippets.stream()
                                                        .map(ms -> List.of(ms.name(), ms.signature(), ms.source()))
                                                        .collect(Collectors.toList()))));
                                    } catch(IOException e) {
                                        logger.log(Level.INFO, "error sending method headers", e);
                                    }
                                    // TODO: only send methods that changed
                                }
                        )
                ),"/exercises/** /methods");

        registry.addHandler(
                new ExerciseWebsocketHandler(exercises, "stdout listener",
                        (tool, session) -> tool.attachStdoutListener(
                                (msg) -> {
                                    if(session.isOpen()) try {
                                        session.sendMessage(new TextMessage(msg));
                                    } catch(IOException e) {
                                        logger.log(Level.INFO, "error sending stdout", e);
                                    }
                                }
                        )
                ),"/exercises/** /stdout");

        registry.addHandler(
                new ExerciseWebsocketHandler(exercises, "goals listener",
                        (tool, session) -> tool.attachGoalsListener(
                                (status) -> {
                                    if(session.isOpen()) try {
                                        GsonBuilder builder = new GsonBuilder();
                                        Gson gson = builder.create();
                                        var msg = gson.toJson(List.of(testId,reason,progress));
                                        session.sendMessage(new TextMessage(msg));
                                    } catch(IOException e) {
                                        logger.log(Level.INFO, "error sending goal info", e);
                                    }
                                }
                        )
                ),"/exercises/** /goals");
    }
    */
}
