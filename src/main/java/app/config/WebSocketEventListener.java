package app.config;

import java.time.LocalDateTime;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import app.chat.exception.EntityNotFoundException;
import app.chat.model.Message;
import app.chat.model.User;
import app.chat.service.UserService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor

public class WebSocketEventListener {

//    private final SimpMessagingTemplate messagingTemplate;
//    private final UserService userService;
//
//    @EventListener
//    public void handleWebSocketConnectListener(SessionConnectEvent event) {
//        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
//        Authentication auth = (Authentication) accessor.getUser();
//        
//        if (auth != null) {
//            String username = auth.getName();
//            System.out.println("User connected: " + username);
//        }
//    }
//
//    @EventListener
//    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
//        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
//        Authentication auth = (Authentication) accessor.getUser();
//        
//        if (auth != null) {
//            String username = auth.getName();
//            System.out.println("User disconnected: " + username);
//            
//            try {
//                Message chatMessage = Message.builder()
//                    .type(Message.MessageType.LEAVE)
//                    .senderName(username)
//                    .timestamp(LocalDateTime.now())
//                    .build();
//                
//                messagingTemplate.convertAndSend("/topic/public", chatMessage);
//            } catch (Exception e) {
//                System.err.println("Error handling disconnect: " + e.getMessage());
//            }
//        }
//    }
}