package app.chat.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.chat.exception.EntityNotFoundException;
import app.chat.model.Group;
import app.chat.model.Message;
import app.chat.model.User;
import app.chat.repositories.GroupRepository;
import app.chat.repositories.MessageRepository;
import app.chat.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    @Transactional
    public Message saveMessage(Message message) {
        if (message.getSender() != null) {
            message.setSenderName(message.getSender().getUsername());
            message.setGetSenderId(message.getSender().getId().toString());
        }
        log.info("Saving conversation message: {}", message.getContent());
        return messageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public List<Message> getConversationHistory(Long user1Id, Long user2Id) throws EntityNotFoundException {
        User user1 = userRepository.findById(user1Id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + user1Id));
        User user2 = userRepository.findById(user2Id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + user2Id));
        log.info("Fetching private conversation between users {} and {}", user1Id, user2Id);
        List<Message> messages = messageRepository.findPrivateMessages(user1.getId(), user2.getId());
        messages.forEach(msg -> {
            if (msg.getSender() != null) {
                msg.setSenderName(msg.getSender().getUsername());
                msg.setGetSenderId(msg.getSender().getId().toString());
            }
        });
        return messages;
    }

    @Transactional(readOnly = true)
    public List<Message> getGroupMessages(Long groupId) throws EntityNotFoundException {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id: " + groupId));
        log.info("Fetching group messages for group {}", groupId);
        List<Message> messages = messageRepository.findByGroupId(group.getId());
        messages.forEach(msg -> {
            if (msg.getSender() != null) {
                msg.setSenderName(msg.getSender().getUsername());
                msg.setGetSenderId(msg.getSender().getId().toString());
            }
        });
        return messages;
    }

    @Transactional(readOnly = true)
    public List<Message> getPublicMessages() {
        log.info("Fetching public conversation messages");
        List<Message> messages = messageRepository.findPublicMessages();
        messages.forEach(msg -> {
            if (msg.getSender() != null) {
                msg.setSenderName(msg.getSender().getUsername());
                msg.setGetSenderId(msg.getSender().getId().toString());
            }
        });
        return messages;
    }
}