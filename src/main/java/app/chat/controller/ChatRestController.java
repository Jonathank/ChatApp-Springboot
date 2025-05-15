// app.chat.controller.ChatRestController
package app.chat.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import app.chat.dto.GroupDTO;
import app.chat.dto.UserDTO;
import app.chat.dto.UserUpdateRequest;
import app.chat.exception.AccessDeniedException;
import app.chat.exception.EntityNotFoundException;
import app.chat.model.Group;
import app.chat.model.Image;
import app.chat.model.Message;
import app.chat.model.Message.MessageType;
import app.chat.model.User;
import app.chat.repositories.ImageRepository;
import app.chat.service.GroupService;
import app.chat.service.MessageService;
import app.chat.service.UserService;
import app.config.impl.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/KJN/chatting/app")
@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
public class ChatRestController {

    private final UserService userService;
    private final GroupService groupService;
    private final MessageService messageService;
   // private final ImageService imageService;
    private final SimpMessagingTemplate messagingTemplate; // Added for WebSocket errors
    private final ImageRepository imageRepository;
    // User endpoints
//    @GetMapping("/users")
//    public ResponseEntity<List<UserDTO>> getAllUsers() {
//        return ResponseEntity.ok(userService.getAllUsersExceptCurrent());
//    }
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers(HttpServletRequest request) {
        List<UserDTO> users = userService.getAllUsers(request);
        return ResponseEntity.ok(users);
    }
    @GetMapping("/users/all/online")
    public ResponseEntity<List<User>> getOnlineUsers() {
        return ResponseEntity.ok(userService.getOnlineUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) throws EntityNotFoundException {
        return ResponseEntity.ok(userService.getUserById(id));
    }

   

    @PutMapping("/users/{userId}/update")
    public ResponseEntity<UserUpdateRequest> updateUserProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest user
         
    ) throws AccessDeniedException {
        try {
            UserUpdateRequest updatedUser = userService.updateUserProfile(userId, user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            messagingTemplate.convertAndSendToUser(
                userId.toString(), "/queue/errors", "Failed to update user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(null);
        }
    }

    

    @GetMapping("/users/groups/{id}")
    public ResponseEntity<Group> getGroupById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(groupService.getGroupById(id));
        } catch (EntityNotFoundException e) {
            messagingTemplate.convertAndSendToUser(
                id.toString(), "/queue/errors", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

   

//    @PostMapping("/users/groups")
//    public ResponseEntity<Group> createGroup(@RequestBody Group group) {
//        try {
//            return ResponseEntity.ok(groupService.createGroup(group));
//        } catch (Exception e) {
//            messagingTemplate.convertAndSendToUser(
//                group.getGroupname(), "/queue/errors", "Failed to create group: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                .body(null);
//        }
//    }
//    

   
    
//    @PostMapping("/groups")
//    public ResponseEntity<Group> createGroup(
//        @RequestPart("groupname") String groupname,
//        @RequestPart(value = "image", required = false) MultipartFile image,
//        Authentication authentication
//    ) {
//        log.info("Create group request: groupname={}, auth={}", groupname, authentication);
//        Group group = userService.createGroup(groupname, image, authentication);
//        return ResponseEntity.ok(group);
//    }

    //@PreAuthorize("hasRole('ADMIN')")
//    @PostMapping("/users/groups/{groupId}/members")
//    public ResponseEntity<Void> addUsersToGroup(
//        @PathVariable Long groupId,
//        @RequestBody Map<String, List<Long>> requestBody) {
//        try {
//            List<Long> userIds = requestBody.get("userIds");
//            if (userIds == null || userIds.isEmpty()) {
//                return ResponseEntity.badRequest().build();
//            }
//            
//            for (Long userId : userIds) {
//                groupService.addUserToGroup(userId, groupId);
//                
//                // Send notification for each user added
//                messagingTemplate.convertAndSend("/topic/group/" + groupId, Message.builder()
//                    .sender(userService.getUserById(userId))
//                    .content("User added to group")
//                    .type(MessageType.GROUP_ADD)
//                    .timestamp(LocalDateTime.now())
//                    .isGroupMessage(true)
//                    .group(groupService.getGroupById(groupId))
//                    .build());
//            }
//            
//            return ResponseEntity.ok().build();
//        } catch (EntityNotFoundException e) {
//            // Log the error
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        }
//        }
//    
//    //@PreAuthorize("hasRole('ADMIN')")
//    @DeleteMapping("/admin/groups/{groupId}/users/{userId}")
//    public ResponseEntity<Void> removeUserFromGroup(@PathVariable Long groupId, @PathVariable Long userId) {
//        try {
//            groupService.removeUserFromGroup(userId, groupId);
//            messagingTemplate.convertAndSend("/topic/group/" + groupId, Message.builder()
//                .sender(userService.getUserById(userId))
//                .content("User removed from group")
//                .type(MessageType.GROUP_REMOVE)
//                .timestamp(LocalDateTime.now())
//                .isGroupMessage(true)
//                .group(groupService.getGroupById(groupId))
//                .build());
//            return ResponseEntity.ok().build();
//        } catch (EntityNotFoundException e) {
//            messagingTemplate.convertAndSendToUser(
//                userId.toString(), "/queue/errors", e.getMessage());
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        }
//    }

    // Message endpoints
    @GetMapping("/users/messages/private")
    public ResponseEntity<List<Message>> getPrivateMessages(
            @RequestParam Long user1Id, @RequestParam Long user2Id) {
        try {
            return ResponseEntity.ok(messageService.getConversationHistory(user1Id, user2Id));
        } catch (EntityNotFoundException e) {
            messagingTemplate.convertAndSendToUser(
                user1Id.toString(), "/queue/errors", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/users/messages/group/{groupId}")
    public ResponseEntity<List<Message>> getGroupMessages(@PathVariable Long groupId) throws EntityNotFoundException {
        return ResponseEntity.ok(messageService.getGroupMessages(groupId));
    }

    @GetMapping("/users/messages/public")
    public ResponseEntity<List<Message>> getPublicMessages() {
        return ResponseEntity.ok(messageService.getPublicMessages());
    }

    // Image endpoints

    @PostMapping("/user/image/{userId}/upload")
    public ResponseEntity<UserDTO> saveUserImage(
            @PathVariable Long userId,
            @RequestParam("image") MultipartFile file,
            Authentication authentication) throws Exception {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        String email = authentication.getName(); // Gets the username from the principal
        User creator = userService.getUserByEmail(email);
          
        UserDTO updatedUser = userService.saveUserImage(creator.getId(), file);
        return ResponseEntity.status(HttpStatus.CREATED).body(updatedUser);
    }

    @PutMapping("/user/image/{imageId}/update")
    public ResponseEntity<UserDTO> updateUserImage(
            @PathVariable Long imageId,
            @RequestParam("image") MultipartFile file
            ) throws Exception {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        UserDTO updatedUser = userService.updateUserImage(imageId, file);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{userId}/image")
    public ResponseEntity<UserDTO> removeUserImage(@PathVariable Long userId) throws EntityNotFoundException {
        UserDTO updatedUser = userService.removeUserImage(userId);
        return ResponseEntity.ok(updatedUser);
    }

   
//    @GetMapping("/users/image/download/{imageId}")
//    public ResponseEntity<byte[]> getImage(@PathVariable Long imageId) throws SQLException, EntityNotFoundException{
//        Image image = imageService.getImageById(imageId);
//              
//        Blob blob = image.getImage();
//        byte[] imageBytes = blob.getBytes(1, (int) blob.length());
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.IMAGE_JPEG); // Adjust based on image type
//        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
//    }
    
    
    @GetMapping("/users/image/download/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        try {
            Image image = imageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Image not found with id: " + id));
            byte[] imageData = image.getImage();
            if (imageData == null) {
                throw new EntityNotFoundException("No image data found for id: " + id);
            }
            HttpHeaders headers = new HttpHeaders();
            String contentType =   "image/jpeg";
            headers.setContentType(MediaType.parseMediaType(contentType));
            return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            messagingTemplate.convertAndSendToUser(
                id.toString(), "/queue/errors", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error retrieving image with id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
  
    
}