package net.sourcedestination.codecafe;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketsConfiguration implements WebSocketConfigurer {

    @Autowired
    private JshellWebsocketHandler jshellHandler;

    @Autowired
    private VariableDefinitionsWebsocketHandler varsHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(jshellHandler, "/jshell/**");
        registry.addHandler(varsHandler, "/vars/**");
        // TODO: add URL params for lesson identification
        // lessons may have restrictions on code used, pre-loaded code, or listeners for certain events
    }

    // TODO: new websocket endpoint for listening to command history
    // TODO: new websocket endpoint for listening to variable defintions / values
    // TODO: new websocket endpoint for listening to / updating method definitions
    // TODO: new websocket endpoint for listening to unit test results, success triggers
}