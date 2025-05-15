package app.chat.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import app.chat.dto.ImageDTO;
import app.chat.dto.UserCreateRequest;
import app.chat.dto.UserDTO;
import app.chat.dto.UserUpdateRequest;
import app.chat.exception.EntityNotFoundException;
import app.chat.model.Image;
import app.chat.model.User;
import app.chat.repositories.UserRepository;
import app.config.jwt.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final ImageService imageService;
    private final UserRepository userRepository;
    private final JwtService jwtService;

//    @Cacheable("users")
//    public User getUserByEmail(String email) {
//        return userRepository.findByEmail(email);
//    }
//
//    @Cacheable("users")
//    public User getUserById(Long id) {
//        return userRepository.findById(id).orElse(null);
//    }
//    
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::toUserDTO)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers(HttpServletRequest request) {
        // Extract JWT token from the Authorization header
        String authHeader = request.getHeader("Authorization");
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        
        // Get current user ID from the token
        String currentUserId = jwtService.getEmailFromToken(token);
        User theuser = userRepository.findByEmail(currentUserId);
        // Find all users and filter out the current user
        List<User> users = userRepository.findAll();
        return users.stream()
            .filter(user -> !user.getId().equals(theuser.getId()))
            .map(this::toUserDTO)
            .collect(Collectors.toList());
    }
    

//    @Transactional(readOnly = true)
//    public User getUserById(Long id) throws EntityNotFoundException {
//        return userRepository.findByIdWithImage(id)
//                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
//    }

   public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElse(null);
    }
    @Transactional(readOnly = true)
    public List<User> getOnlineUsers() {
        return userRepository.findByOnlineTrue();
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

   // @Cacheable(value = "users", key = "#email")
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) throws EntityNotFoundException {
       User user = userRepository.findByEmail(email);
       if(user != null) {
	   return user; 
       }
	throw new EntityNotFoundException("user not found with email "+ email);
                
    }

    @Transactional
    public void updateUserOnlineStatus(Long userId, boolean online) {
        userRepository.updateUserOnlineStatus(userId, online);
    }

    @Transactional(readOnly = true)
    public String getUsernameById(Long userId) {
        return userRepository.findUsernameById(userId);
    }

    public User convertToUser(UserCreateRequest userReq) throws IOException {
        User user = new User();
        user.setUsername(userReq.getUsername());
        user.setEmail(userReq.getEmail());
        user.setPassword(userReq.getPassword()); // Consider encoding password
        return user;
    }

    @Transactional
    public UserUpdateRequest updateUserProfile(Long id, UserUpdateRequest userReq) throws IOException, EntityNotFoundException {
        log.info("Updating user: {}", userReq.getUsername());
        User existingUser = getUserById(id);
        existingUser.setUsername(userReq.getUsername());
        existingUser.setEmail(userReq.getEmail());
        User updatedUser = userRepository.save(existingUser);

        UserUpdateRequest updatedUserDto = new UserUpdateRequest();
        updatedUserDto.setUsername(updatedUser.getUsername());
        updatedUserDto.setEmail(updatedUser.getEmail());
        return updatedUserDto;
    }

    @Transactional
    public UserDTO saveUserImage(Long userId, MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file cannot be empty");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        ImageDTO imageDTO = imageService.saveImage(user, file);
        Image image = new Image();
        image.setId(imageDTO.getId());
        image.setDownloadUrl(imageDTO.getDownloadUrl());
        image.setUser(user);
        user.setImage(image);
        User savedUser = userRepository.save(user);
        return toUserDTO(savedUser);
    }

    @Transactional
    public UserDTO updateUserImage(Long userId, MultipartFile file) throws Exception {
      
	
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        if (user.getImage() == null) {
           return  saveUserImage(userId, file);
        }
        
        ImageDTO imageDTO = imageService.updateImage(user.getImage().getId(), file);
        user.getImage().setDownloadUrl(imageDTO.getDownloadUrl());
        User updatedUser = userRepository.save(user);
        return toUserDTO(updatedUser);
        
       
    }

    @Transactional
    public UserDTO removeUserImage(Long userId) throws EntityNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        if (user.getImage() != null) {
            imageService.deleteImage(user.getImage().getId());
            user.setImage(null);
        }
        User savedUser = userRepository.save(user);
        return toUserDTO(savedUser);
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
    
}
