package com.notificationService.config;

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
        // Add BOTH paths just to be completely safe!
        registry.addEndpoint("/ws-notifications", "/notification/ws-notifications")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    // --- SECURITY INTERCEPTOR ---
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor.getCommand() == null) return message;

                // 1. Check if user is trying to CONNECT
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String userId = accessor.getFirstNativeHeader("userId");
                    System.out.println("🟢 [WS INTERCEPTOR] Connection attempt from User ID: " + userId);

                    if (userId == null) {
                        System.out.println("🔴 [WS INTERCEPTOR] Connection REJECTED: Missing User Identity");
                        throw new IllegalArgumentException("Missing User Identity");
                    }

                    accessor.getSessionAttributes().put("userId", userId);
                    System.out.println("✅ [WS INTERCEPTOR] User " + userId + " Connected Successfully!");
                }

                // 2. Check if user is trying to SUBSCRIBE
                if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    String destination = accessor.getDestination();
                    String sessionUserId = (String) accessor.getSessionAttributes().get("userId");

                    System.out.println("📡 [WS INTERCEPTOR] User " + sessionUserId + " subscribing to: " + destination);

                    if (destination != null && !destination.endsWith("/" + sessionUserId)) {
                        System.out.println("🔴 [WS INTERCEPTOR] Subscription REJECTED for User " + sessionUserId);
                        throw new IllegalArgumentException("Unauthorized Subscription");
                    }
                    System.out.println("✅ [WS INTERCEPTOR] Subscription APPROVED!");
                }
                return message;
            }
        });
    }
}