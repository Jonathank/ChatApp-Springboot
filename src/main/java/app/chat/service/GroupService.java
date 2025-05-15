package app.chat.service;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import app.chat.domain.USER_ROLE;
import app.chat.dto.GroupDTO;
import app.chat.dto.ImageDTO;
import app.chat.dto.UserDTO;
import app.chat.exception.AccessDeniedException;
import app.chat.exception.EntityNotFoundException;
import app.chat.model.Group;
import app.chat.model.Image;
import app.chat.model.User;
import app.chat.repositories.GroupRepository;
import app.chat.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {

    private final GroupRepository groupRepository;
    private final ImageService imageService;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Group getGroupById(Long id) throws EntityNotFoundException {
        return groupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Group getGroupByName(String groupname) throws EntityNotFoundException {
        return groupRepository.findByGroupname(groupname)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with name: " + groupname));
    }

    @Transactional(readOnly = true)
    public List<GroupDTO> getUserGroups(Long userId) throws EntityNotFoundException {
        List<Group> groups = groupRepository.findByMembersId(userId);
        log.info("Fetched groups for user {}: {}", userId, groups.size());
        return groups.stream()
                .map(this::toGroupDTO)
                .collect(Collectors.toList());
    }

    public GroupDTO toGroupDTO(Group group) {
        List<UserDTO> members = group.getMembers().stream()
                .map(user -> new UserDTO(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getImage() != null ? new ImageDTO(user.getImage().getId(), user.getImage().getDownloadUrl()) : null,
                        user.isOnline(),
                        user.getRole().name(),
                        user.getStatus().name()
                ))
                .collect(Collectors.toList());
        return new GroupDTO(
                group.getId(),
                group.getGroupname(),
                group.getImage() != null ? new ImageDTO(group.getImage().getId(), group.getImage().getDownloadUrl()) : null,
                members,
                group.getCreator().getId(),
                null
        );
    }

    @Transactional
    public GroupDTO createGroup(GroupDTO groupDTO, Long creatorId) throws EntityNotFoundException {
        if (groupDTO.getGroupname() == null || groupDTO.getGroupname().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group name cannot be empty");
        }
        if (groupRepository.findByGroupname(groupDTO.getGroupname()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group name already exists: " + groupDTO.getGroupname());
        }
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + creatorId));

        Group group = Group.builder()
                .groupname(groupDTO.getGroupname())
                .isPublic(false)
                .creator(creator)
                .members(new HashSet<>())
                .groupAdmins(new HashSet<>())
                .build();

        group.getMembers().add(creator);
        group.getGroupAdmins().add(creator); // Creator is automatically an admin
        creator.getGroups().add(group);
        creator.getAdminGroups().add(group);

        log.info("Before save: Group name: {}, Members: {}, Admins: {}", 
                group.getGroupname(), group.getMembers().size(), group.getGroupAdmins().size());

        Group savedGroup = groupRepository.save(group);
        groupRepository.flush();
        log.info("After save: Group ID: {}, Members: {}, Admins: {}", 
                savedGroup.getId(), savedGroup.getMembers().size(), savedGroup.getGroupAdmins().size());

        return toGroupDTO(savedGroup);
    }

    @Transactional
    public GroupDTO updateGroup(Long groupId, GroupDTO groupDTO, Long userId) 
            throws EntityNotFoundException, AccessDeniedException {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id: " + groupId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        if (!group.getGroupAdmins().contains(user)) {
            throw new AccessDeniedException("Only group admins can update the group");
        }
        if (groupDTO.getGroupname() != null && !groupDTO.getGroupname().trim().isEmpty()) {
            if (groupRepository.findByGroupname(groupDTO.getGroupname()).isPresent() &&
                !groupDTO.getGroupname().equals(group.getGroupname())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group name already exists: " + groupDTO.getGroupname());
            }
            group.setGroupname(groupDTO.getGroupname());
        }
        Group updatedGroup = groupRepository.save(group);
        groupRepository.flush();
        log.info("Updated group ID: {}, Name: {}", updatedGroup.getId(), updatedGroup.getGroupname());

        return toGroupDTO(updatedGroup);
    }

    @Transactional
    public void deleteGroup(Long groupId, Long userId) throws EntityNotFoundException, AccessDeniedException {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id: " + groupId));
        User requestingUser = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        if (!group.getGroupAdmins().contains(requestingUser)) {
            throw new AccessDeniedException("Only group admins can delete group");
        }
        group.getMembers().forEach(user -> user.getGroups().remove(group));
        groupRepository.delete(group);
        log.info("Deleted group ID: {}", groupId);
    }

    @Transactional
    public void addUserToGroup(Long userId, Long groupId, Long requestingUserId) 
            throws EntityNotFoundException, AccessDeniedException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id: " + groupId));
        User requestingUser = userRepository.findById(requestingUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + requestingUserId));

        if (!group.getGroupAdmins().contains(requestingUser)) {
            throw new AccessDeniedException("Only group admins can add members");
        }
        if (group.getMembers().contains(user)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already a member of the group");
        }
        group.getMembers().add(user);
        user.getGroups().add(group);
        userRepository.save(user);
        groupRepository.save(group);
        log.info("Added user {} to group {}", userId, groupId);
    }

    @Transactional
    public void addUsersToGroup(List<Long> userIds, Long groupId, Long requestingUserId) 
            throws EntityNotFoundException, AccessDeniedException {
        if (userIds == null || userIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User IDs list cannot be empty");
        }
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id: " + groupId));
        User requestingUser = userRepository.findById(requestingUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + requestingUserId));

        if (!group.getGroupAdmins().contains(requestingUser)) {
            throw new AccessDeniedException("Only group admins can add members");
        }
        for (Long userId : userIds) {
            User userToAdd = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
            if (group.getMembers().contains(userToAdd)) {
                log.warn("User {} is already a member of group {}", userId, groupId);
                continue;
            }
            group.getMembers().add(userToAdd);
            userToAdd.getGroups().add(group);
            userRepository.save(userToAdd);
        }
        groupRepository.save(group);
        log.info("Added {} users to group {}", userIds.size(), groupId);
    }

    @Transactional
    public void removeUserFromGroup(Long userId, Long groupId, Long requestingUserId) 
            throws EntityNotFoundException, AccessDeniedException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id: " + groupId));
        User requestingUser = userRepository.findById(requestingUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + requestingUserId));

        if (!group.getGroupAdmins().contains(requestingUser)) {
            throw new AccessDeniedException("Only group admins can remove members");
        }
        if (!group.getMembers().contains(user)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not a member of the group");
        }
        group.getMembers().remove(user);
        user.getGroups().remove(group);
        userRepository.save(user);
        groupRepository.save(group);
        log.info("Removed user {} from group {}", userId, groupId);
    }

    @Transactional
    public void addGroupAdmin(Long groupId, Long userId, Long requestingUserId) 
            throws EntityNotFoundException, AccessDeniedException {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id: " + groupId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        User requestingUser = userRepository.findById(requestingUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + requestingUserId));

        if (!group.getGroupAdmins().contains(requestingUser)) {
            throw new AccessDeniedException("Only group admins can add other admins");
        }
        if (group.getGroupAdmins().contains(user)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already a group admin");
        }
        if (!group.getMembers().contains(user)) {
            group.getMembers().add(user);
            user.getGroups().add(group);
        }
        group.getGroupAdmins().add(user);
        user.getAdminGroups().add(group);
        userRepository.save(user);
        groupRepository.save(group);
        log.info("Added user {} as admin for group {}", userId, groupId);
    }

    @Transactional
    public void removeGroupAdmin(Long groupId, Long userId, Long requestingUserId) 
            throws EntityNotFoundException, AccessDeniedException {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id: " + groupId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        User requestingUser = userRepository.findById(requestingUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + requestingUserId));

        if (!group.getGroupAdmins().contains(requestingUser)) {
            throw new AccessDeniedException("Only group admins can remove other admins");
        }
        if (!group.getGroupAdmins().contains(user)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not a group admin");
        }
        if (group.getCreator().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot remove the group creator as admin");
        }
        group.getGroupAdmins().remove(user);
        user.getAdminGroups().remove(group);
        userRepository.save(user);
        groupRepository.save(group);
        log.info("Removed user {} as admin for group {}", userId, groupId);
    }

    @Transactional(readOnly = true)
    public Set<UserDTO> getGroupMembers(Long groupId) throws EntityNotFoundException {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id: " + groupId));
        
        // Convert each member to UserDTO and collect into a Set
        return group.getMembers().stream()
                .map(this::toUserDTO)
                .collect(Collectors.toSet());
    }
    
    public UserDTO toUserDTO(User user) {
	    ImageDTO imageDTO = user.getImage() != null
	            ? new ImageDTO(user.getImage().getId(), user.getImage().getDownloadUrl())
	            : null;
	    return new UserDTO(
	            user.getId(),
	            user.getUsername(),
	            user.getEmail(),
	            imageDTO,
	            user.isOnline(),
	            user.getRole().name(),
	            user.getStatus().name()
	    );
	}
    
    @Transactional
    public GroupDTO saveGroupImage(Long groupId, MultipartFile file, Long userId) 
            throws IOException, EntityNotFoundException, AccessDeniedException {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file cannot be empty");
        }
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id: " + groupId));
        User requestingUser = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        if (!group.getGroupAdmins().contains(requestingUser)) {
            throw new AccessDeniedException("Only group admins can add group image");
        }
        try {
            ImageDTO imageDTO = imageService.saveImage(group, file);
            Image image = new Image();
            image.setId(imageDTO.getId());
            image.setDownloadUrl(imageDTO.getDownloadUrl());
            group.setImage(image);
            return toGroupDTO(groupRepository.save(group));
        } catch (IOException e) {
            log.error("Failed to save image for group {}", groupId, e);
            throw new IOException("Failed to save group image", e);
        }
    }

    @Transactional
    public GroupDTO updateGroupImage(Long groupId, MultipartFile file, Long userId) 
            throws IOException, EntityNotFoundException, AccessDeniedException {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file cannot be empty");
        }
        Group group = groupRepository.findByIdWithAdmins(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id: " + groupId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        if (!group.getGroupAdmins().contains(user)) {
            throw new AccessDeniedException("Only group admins can update the group image");
        }
        if (group.getImage() == null) {
            return saveGroupImage(groupId, file, userId);
        }
        ImageDTO imageDTO = imageService.updateImage(group.getImage().getId(), file);
        group.getImage().setDownloadUrl(imageDTO.getDownloadUrl());
        Group updatedGroup = groupRepository.save(group);
        groupRepository.flush();
        log.info("Updated group image for ID: {}", updatedGroup.getId());
        return toGroupDTO(updatedGroup);
    }

    @Transactional
    public void removeGroupImage(Long groupId, Long userId) throws EntityNotFoundException, AccessDeniedException {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id: " + groupId));
        User requestingUser = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        if (!group.getGroupAdmins().contains(requestingUser)) {
            throw new AccessDeniedException("Only group admins can update group image");
        }
        if (group.getImage() != null) {
            imageService.deleteImage(group.getImage().getId());
            group.setImage(null);
            groupRepository.save(group);
            log.info("Removed image for group ID: {}", groupId);
        }
    }

    @Transactional(readOnly = true)
    public boolean isGroupMember(Long groupId, Long userId) throws EntityNotFoundException {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id: " + groupId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        return group.isPublic() || group.getMembers().contains(user);
    }

    @Transactional(readOnly = true)
    public boolean isGroupCreator(Long groupId, Long userId) throws EntityNotFoundException {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id: " + groupId));
        return group.getCreator() != null && group.getCreator().getId().equals(userId);
    }

    @Transactional(readOnly = true)
    public boolean isGroupAdmin(Long groupId, Long userId) throws EntityNotFoundException {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id: " + groupId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        return group.getGroupAdmins().contains(user);
    }

    private boolean hasAdminRole(Long userId) throws EntityNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        return user.getRole() == USER_ROLE.ADMIN;
    }
}