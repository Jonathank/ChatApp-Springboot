package app.chat.controller;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import app.chat.dto.GroupDTO;
import app.chat.dto.UserDTO;
import app.chat.exception.AccessDeniedException;
import app.chat.exception.EntityNotFoundException;
import app.chat.model.User;
import app.chat.service.GroupService;
import app.chat.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/KJN/chatting/app")
@RequiredArgsConstructor
@Slf4j
public class GroupController {

    private final GroupService groupService;
    private final UserService userService;


    @PostMapping("/create/group")
    public ResponseEntity<GroupDTO> createGroup(@RequestBody GroupDTO groupDTO, Authentication authentication) 
            throws EntityNotFoundException {
        // Extract username from Authentication
        String email = authentication.getName(); // Gets the username from the principal

        // Fetch the app.chat.model.User from the database
        User creator = userService.getUserByEmail(email);
                //.orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

        // Create the group using the creator's ID
        GroupDTO createdGroup = groupService.createGroup(groupDTO, creator.getId());
        return ResponseEntity.ok(createdGroup);
    }
    
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDTO> getGroup(@PathVariable Long groupId) throws EntityNotFoundException {
        GroupDTO groupDTO = groupService.toGroupDTO(groupService.getGroupById(groupId));
        return ResponseEntity.ok(groupDTO);
    }

    @GetMapping("/user/groups")
    public ResponseEntity<List<GroupDTO>> getUserGroups(Authentication authentication) throws EntityNotFoundException {
	
        String email = authentication.getName(); // Gets the username from the principal
        User creator = userService.getUserByEmail(email);
          
        List<GroupDTO> groups = groupService.getUserGroups(creator.getId());
        return ResponseEntity.ok(groups);
    }

    @PutMapping("/groups/{groupId}")
    public ResponseEntity<GroupDTO> updateGroup(
	    @PathVariable Long groupId, 
	    @RequestBody GroupDTO groupDTO, 
            Authentication authentication) throws EntityNotFoundException, AccessDeniedException {
	String email = authentication.getName(); // Gets the username from the principal
        User creator = userService.getUserByEmail(email);
          
        GroupDTO updatedGroup = groupService.updateGroup(groupId, groupDTO, creator.getId());
        return ResponseEntity.ok(updatedGroup);
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long groupId, Authentication authentication) 
            throws EntityNotFoundException, AccessDeniedException {
	String email = authentication.getName(); // Gets the username from the principal
        User creator = userService.getUserByEmail(email);
          
        groupService.deleteGroup(groupId, creator.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/groups/{groupId}/members")
    public ResponseEntity<Void> addUsersToGroup(
	    @PathVariable Long groupId, 
	    @RequestBody List<Long> userIds, 
            Authentication authentication) throws EntityNotFoundException, AccessDeniedException {
	String email = authentication.getName(); // Gets the username from the principal
        User creator = userService.getUserByEmail(email);
          
        groupService.addUsersToGroup(userIds, groupId,creator.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Void> removeUserFromGroup(
	    @PathVariable Long groupId, 
	    @PathVariable Long userId, 
            Authentication authentication) throws EntityNotFoundException, AccessDeniedException {
	String email = authentication.getName(); // Gets the username from the principal
        User creator = userService.getUserByEmail(email);
          
        groupService.removeUserFromGroup(userId, groupId, creator.getId());
        return ResponseEntity.ok().build();
    }
  
    @PostMapping("/groups/{groupId}/admins")
    public ResponseEntity<Void> addGroupAdmin(@PathVariable Long groupId, @RequestBody Long userId, 
            Authentication authentication) throws EntityNotFoundException, AccessDeniedException {
	String email = authentication.getName(); // Gets the username from the principal
        User creator = userService.getUserByEmail(email);
          
        groupService.addGroupAdmin(groupId, userId, creator.getId());
        return ResponseEntity.ok().build();
    }
// http://localhost:8080/KJN/chatting/app/groups/4/members
    @GetMapping("/groups/{groupId}/get/members")
    public ResponseEntity<Set<UserDTO>> getGroupMembers(
	    @PathVariable Long groupId,
	    Authentication authentication) throws EntityNotFoundException {
	
        String email = authentication.getName(); // Gets the username from the principal
        User creator = userService.getUserByEmail(email);
          
        Set<UserDTO> groups = groupService.getGroupMembers(groupId);
        return ResponseEntity.ok(groups);
    }
    
    @DeleteMapping("/{groupId}/admins/{userId}")
    public ResponseEntity<Void> removeGroupAdmin(@PathVariable Long groupId, @PathVariable Long userId, 
            Authentication authentication) throws EntityNotFoundException, AccessDeniedException {
	String email = authentication.getName(); // Gets the username from the principal
        User creator = userService.getUserByEmail(email);
          
        groupService.removeGroupAdmin(groupId, userId, creator.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{groupId}/image")
    public ResponseEntity<GroupDTO> uploadGroupImage(
	    @PathVariable Long groupId, 
            @RequestParam("file") MultipartFile file,
            Authentication authentication) 
            throws IOException, EntityNotFoundException, AccessDeniedException {
	String email = authentication.getName(); // Gets the username from the principal
        User creator = userService.getUserByEmail(email);
          
        GroupDTO groupDTO = groupService.saveGroupImage(groupId, file, creator.getId());
        return ResponseEntity.ok(groupDTO);
    }

    @PutMapping("/groups/{groupId}/image")
    public ResponseEntity<GroupDTO> updateGroupImage(
	    @PathVariable Long groupId, 
            @RequestParam("file") MultipartFile file, 
            Authentication authentication) 
            throws IOException, EntityNotFoundException, AccessDeniedException {
	String email = authentication.getName(); // Gets the username from the principal
        User creator = userService.getUserByEmail(email);
          
        GroupDTO groupDTO = groupService.updateGroupImage(groupId, file, creator.getId());
        return ResponseEntity.ok(groupDTO);
    }
    
    @GetMapping("/groups/{groupId}/isAdmin")
    public ResponseEntity<Boolean> isGroupAdmin(
	    @PathVariable Long groupId, 
	    Authentication authentication) 
            throws EntityNotFoundException {
	String email = authentication.getName(); // Gets the username from the principal
        User creator = userService.getUserByEmail(email);
          
        boolean isAdmin = groupService.isGroupAdmin(groupId, creator.getId());
        return ResponseEntity.ok(isAdmin);
    }

    @DeleteMapping("/{groupId}/image")
    public ResponseEntity<Void> removeGroupImage(
	    @PathVariable Long groupId, 
	    Authentication authentication) 
            throws EntityNotFoundException, AccessDeniedException {
	String email = authentication.getName(); // Gets the username from the principal
        User creator = userService.getUserByEmail(email);
          
        groupService.removeGroupImage(groupId, creator.getId());
        return ResponseEntity.ok().build();
    }

   
}