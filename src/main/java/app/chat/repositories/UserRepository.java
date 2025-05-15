package app.chat.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import app.chat.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByUsernameNot(String username);

    Optional<User> findById(Long userId);

    public List<User> findByIdNotIn(List<Long> memberIds);
 
    List<User> findByOnlineTrue();
    
    @Modifying
    @Query("UPDATE User u SET u.online = :status WHERE u.id = :userId")
    void updateUserOnlineStatus(@Param("userId") Long userId, @Param("status") boolean status);

    User findByEmail(String email);
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.image WHERE u.id = :id")
    Optional<User> findByIdWithImage(Long id);

    String findUsernameById(Long userId);

   // User findUserByEmail(String email);
}