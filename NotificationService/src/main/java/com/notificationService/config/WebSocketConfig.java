package com.notificationService.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.*;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-notifications", "/notification/ws-notifications")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor == null || accessor.getCommand() == null) return message;

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String userId = accessor.getFirstNativeHeader("userId");

                    if (userId == null) {
                        log.warn("🔴 [WS INTERCEPTOR] Connection REJECTED: Missing User Identity");
                        throw new IllegalArgumentException("Missing User Identity");
                    }

                    accessor.getSessionAttributes().put("userId", userId);
                    log.info("🟢 [WS INTERCEPTOR] User {} Connected Successfully!", userId);
                }

                if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    String destination = accessor.getDestination();
                    String sessionUserId = (String) accessor.getSessionAttributes().get("userId");

                    if (destination != null && !destination.endsWith("/" + sessionUserId)) {
                        log.warn("🔴 [WS INTERCEPTOR] Subscription REJECTED for User {}. Attempted path: {}", sessionUserId, destination);
                        throw new IllegalArgumentException("Unauthorized Subscription");
                    }
                    log.info("📡 [WS INTERCEPTOR] Subscription APPROVED for User {} to {}", sessionUserId, destination);
                }
                return message;
            }
        });
    }
}