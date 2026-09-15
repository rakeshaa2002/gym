package com.fitnexus.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Clients subscribe to /user/queue/call to receive private call signals.
        config.enableSimpleBroker("/queue");
        // Client sends signals to /app/call/signal; routed to @MessageMapping handlers.
        config.setApplicationDestinationPrefixes("/app");
        // Prefix for user-specific queues (/user/{userId}/queue/call).
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket endpoint for clients to connect to.
        registry.addEndpoint("/ws/call")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        // Plain WebSocket (no SockJS) for modern browsers.
        registry.addEndpoint("/ws/call")
                .setAllowedOriginPatterns("*");
    }
}
