package net.sourcedestination.codecafe;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketsConfiguration implements WebSocketConfigurer {

    @Bean
    public WebSocketHandler JshellWebsocketHandler() {
        return new JshellWebsocketHandler();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(JshellWebsocketHandler(), "/jshell/**");
        // TODO: add URL params for lesson identification
        // lessons may have restrictions on code used, pre-loaded code, or listeners for certain events
    }

    // TODO: new websocket endpoint for listening to command history
    // TODO: new websocket endpoint for listening to variable defintions / values
    // TODO: new websocket endpoint for listening to / updating method definitions
    // TODO: new websocket endpoint for listening to unit test results, success triggers
}