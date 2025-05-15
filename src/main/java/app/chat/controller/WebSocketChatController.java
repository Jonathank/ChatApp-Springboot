package app.chat.controller;
import java.security.Principal;
import java.time.LocalDateTime;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import app.chat.model.Group;
import app.chat.model.Message;
import app.chat.model.Message.MessageType;
import app.chat.model.User;
import app.chat.service.GroupService;
import app.chat.service.MessageService;
import app.chat.service.UserService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class WebSocketChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;
    private final GroupService groupService;
    private final MessageService messageService;

    // Helper method to get user by ID from the principal
    private Long getUserFromPrincipal(Principal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("No Principal provided in WebSocket session");
        }
        String userId = principal.getName();
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("No user ID provided in Principal");
        }
        try {
            return Long.valueOf(userId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid user ID format: " + userId, e);
        }
    }
    
    // Helper method for error handling
    private void handleError(Exception e, String errorMessage, String destination) {
        System.err.println(errorMessage + ": " + e.getMessage());
        e.printStackTrace();
        messagingTemplate.convertAndSend(destination, errorMessage + ": " + e.getMessage());
    }

    
    @MessageMapping("/chat.join")
    public void join(@Payload Message chatMessage, Principal principal) {
        try {
            System.out.println("Join request received, Principal: " + (principal != null ? principal.getName() : "null"));
            Long userId = getUserFromPrincipal(principal);
            User user = userService.getUserById(userId);
            if (user == null) {
                throw new IllegalArgumentException("User not found with ID: " + userId);
            }
           
            Message message = Message.builder()
                    .sender(user)
                    .content(user.getUsername() + " joined the chat.")
                    .type(MessageType.JOIN)
                    .timestamp(LocalDateTime.now())
                    .isGroupMessage(false)
                    .build();
            messagingTemplate.convertAndSend("/topic/public", message);
        } catch (Exception e) {
            handleError(e, "Error joining chat", "/topic/public");
        }
    }

    @MessageMapping("/chat.sendMessage/{recipientId}")
    public void sendMessage(
            @DestinationVariable Long recipientId,
            @Payload Message chatMessage,
            Principal principal) {
        try {
            Long userId = getUserFromPrincipal(principal);
            User sender = userService.getUserById(userId);
            if (sender == null) {
                throw new IllegalArgumentException("User not found with ID: " + userId);
            }
           // User sender = getUserFromPrincipal(principal);
            User recipient = userService.getUserById(recipientId);

            if (recipient == null) {
                throw new IllegalArgumentException("Recipient not found");
            }
            Message message = Message.builder()
                    .sender(sender)
                    .recipient(recipient)
                    .content(chatMessage.getContent())
                    .type(MessageType.CHAT)
                    .timestamp(LocalDateTime.now())
                    .isGroupMessage(false)
                    .build();

            Message saved = messageService.saveMessage(message);
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(recipient.getId()),
                    "/queue/messages",
                    saved
            );
        } catch (Exception e) {
            handleError(e, "Error sending message", "/queue/errors");
        }
    }

    @MessageMapping("/chat.sendGroupMessage/{groupId}")
    public void sendGroupMessage(
            @DestinationVariable Long groupId,
            @Payload Message chatMessage,
            Principal principal) {
        try {
            Long userId = getUserFromPrincipal(principal);
            User sender = userService.getUserById(userId);
            if (sender == null) {
                throw new IllegalArgumentException("User not found with ID: " + userId);
            }
            //User sender = getUserFromPrincipal(principal);
            Group group = groupService.getGroupById(groupId);

            if (group == null) {
                throw new IllegalArgumentException("Group not found");
            }

            Message message = Message.builder()
                    .sender(sender)
                    .content(chatMessage.getContent())
                    .type(MessageType.CHAT)
                    .timestamp(LocalDateTime.now())
                    .isGroupMessage(true)
                    .group(group)
                    .build();

            Message saved = messageService.saveMessage(message);
            messagingTemplate.convertAndSend("/topic/group/" + groupId, saved);
        } catch (Exception e) {
            handleError(e, "Error sending group message", "/queue/errors");
        }
    }

    @MessageMapping("/chat.sendMessage")
    public void sendPublicMessage(
            @Payload Message chatMessage,
            Principal principal) {
        try {
            Long userId = getUserFromPrincipal(principal);
            User sender = userService.getUserById(userId);
            if (sender == null) {
                throw new IllegalArgumentException("User not found with ID: " + userId);
            }
           // User sender = getUserFromPrincipal(principal);

            Message message = Message.builder()
                    .sender(sender)
                    .content(chatMessage.getContent())
                    .type(MessageType.CHAT)
                    .timestamp(LocalDateTime.now())
                    .isGroupMessage(false)
                    .build();

            Message saved = messageService.saveMessage(message);
            messagingTemplate.convertAndSend("/topic/public", saved);
        } catch (Exception e) {
            handleError(e, "Error sending public message", "/topic/public");
        }
    }

    @MessageMapping("/chat.typing/{destinationId}")
    public void sendTyping(
            @DestinationVariable String destinationId,
            @Payload Message chatMessage,
            Principal principal) {
        try {
            Long userId = getUserFromPrincipal(principal);
            User sender = userService.getUserById(userId);
            if (sender == null) {
                throw new IllegalArgumentException("User not found with ID: " + userId);
            }
           // User sender = getUserFromPrincipal(principal);

            Message.MessageBuilder messageBuilder = Message.builder()
                    .sender(sender)
                    .content(sender.getUsername() + " is typing...")
                    .type(MessageType.TYPING)
                    .timestamp(LocalDateTime.now())
                    .isGroupMessage(chatMessage.isGroupMessage());

            if (chatMessage.isGroupMessage()) {
                Group group = groupService.getGroupById(Long.parseLong(destinationId));
                if (group == null) {
                    throw new IllegalArgumentException("Group not found");
                }

                messageBuilder.group(group);
                messagingTemplate.convertAndSend("/topic/group/" + destinationId, messageBuilder.build());
            } else if (destinationId.equals("public")) {
                messagingTemplate.convertAndSend("/topic/public", messageBuilder.build());
            } else {
                User recipient = userService.getUserById(Long.parseLong(destinationId));
                if (recipient == null) {
                    throw new IllegalArgumentException("Recipient not found");
                }

                messageBuilder.recipient(recipient);
                messagingTemplate.convertAndSendToUser(
                        String.valueOf(recipient.getId()),
                        "/queue/typing",
                        messageBuilder.build()
                );
            }
        } catch (Exception e) {
            handleError(e, "Error sending typing indicator", "/queue/errors");
        }
    }

    @MessageMapping("/chat.group.update/{groupId}")
    public void updateGroup(
            @DestinationVariable Long groupId,
            @Payload Message chatMessage,
            Principal principal) {
        try {
            Long userId = getUserFromPrincipal(principal);
            User sender = userService.getUserById(userId);
            if (sender == null) {
                throw new IllegalArgumentException("User not found with ID: " + userId);
            }
           // User sender = getUserFromPrincipal(principal);
            Group group = groupService.getGroupById(groupId);

            if (group == null) {
                throw new IllegalArgumentException("Group not found");
            }

            Message message = Message.builder()
                    .sender(sender)
                    .content(chatMessage.getContent())
                    .type(chatMessage.getType())
                    .timestamp(LocalDateTime.now())
                    .isGroupMessage(true)
                    .group(group)
                    .build();

            Message saved = messageService.saveMessage(message);
            messagingTemplate.convertAndSend("/topic/group/" + groupId, saved);
        } catch (Exception e) {
            handleError(e, "Error updating group", "/queue/errors");
        }
    }
}