package app.config;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    //private final JwtService jwtService;
   // private final CustomUserServiceImpl userDetailsService;
    
    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:3000")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    System.out.println("CONNECT frame headers: " + accessor.getMessageHeaders());
                    String userIdHeader = accessor.getFirstNativeHeader("userId");
                    if (userIdHeader != null) {
                        System.out.println("Received userId header: " + userIdHeader);
                        Principal userPrincipal = new UserPrincipal(userIdHeader);
                        // Store Principal in session attributes
                        accessor.getSessionAttributes().put("userPrincipal", userPrincipal);
                        // Create mutable accessor
                        StompHeaderAccessor mutableAccessor = StompHeaderAccessor.create(accessor.getCommand());
                        mutableAccessor.copyHeaders(accessor.getMessageHeaders());
                        mutableAccessor.setUser(userPrincipal);
                        return MessageBuilder.createMessage(message.getPayload(), mutableAccessor.getMessageHeaders());
                    } else {
                        System.err.println("No userId header provided in CONNECT frame");
                    }
                } else {
                    // Retrieve Principal from session attributes for other commands
                    Principal userPrincipal = (Principal) accessor.getSessionAttributes().get("userPrincipal");
                    if (userPrincipal != null) {
                        StompHeaderAccessor mutableAccessor = StompHeaderAccessor.create(accessor.getCommand());
                        mutableAccessor.copyHeaders(accessor.getMessageHeaders());
                        mutableAccessor.setUser(userPrincipal);
                        return MessageBuilder.createMessage(message.getPayload(), mutableAccessor.getMessageHeaders());
                    }
                }
                return message;
            }
        });
    }

class UserPrincipal implements Principal {
    private final String userId;

    public UserPrincipal(String userId) {
        this.userId = userId;
    }

    @Override
    public String getName() {
        return userId;
    }
}

//use with token auth
//@Override
//public void configureClientInboundChannel(ChannelRegistration registration) {
//    registration.interceptors(new ChannelInterceptor() {
//        @Override
//        public Message<?> preSend(Message<?> message, MessageChannel channel) {
//            StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
//            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
//                System.out.println("CONNECT frame headers: " + accessor.getMessageHeaders());
//                String authHeader = accessor.getFirstNativeHeader("Authorization");
//                if (authHeader != null && authHeader.startsWith("Bearer ")) {
//                    String token = authHeader.substring(7);
//                    try {
//                        String email = jwtService.getEmailFromToken(token);
//                        StompHeaderAccessor mutableAccessor = StompHeaderAccessor.create(accessor.getCommand());
//                        mutableAccessor.copyHeaders(accessor.getMessageHeaders());
//                        mutableAccessor.setUser(new UserPrincipal(email));
//                        System.out.println("Authenticated WebSocket user: " + email);
//                        return MessageBuilder.createMessage(message.getPayload(), mutableAccessor.getMessageHeaders());
//                    } catch (Exception e) {
//                        System.err.println("Invalid JWT token: " + e.getMessage());
//                    }
//                } else {
//                    System.err.println("No Authorization header provided in CONNECT frame");
//                }
//            }
//            return message;
//        }
//    });
//}

//i will add this in chatcontroller
//private Long getUserFromPrincipal(Principal principal) {
//    if (principal == null) {
//        throw new IllegalArgumentException("No Principal provided in WebSocket session");
//    }
//    String name = principal.getName();
//    if (name == null || name.isEmpty()) {
//        throw new IllegalArgumentException("No name provided in Principal");
//    }
//    try {
//        return Long.valueOf(name); // If using userId
//    } catch (NumberFormatException e) {
//        User user = userService.getUserByEmail(name); // If using email
//        if (user == null) {
//            throw new IllegalArgumentException("User not found with email: " + name);
//        }
//        return user.getId();
//    }
//}
}