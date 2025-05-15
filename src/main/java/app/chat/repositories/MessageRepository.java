package app.chat.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import app.chat.model.Group;
import app.chat.model.Message;
import app.chat.model.User;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    // For public messages (no recipient AND no group)
    @Query("SELECT m FROM Message m WHERE m.recipient IS NULL AND m.group IS NULL ORDER BY m.timestamp")
    List<Message> findPublicMessages();
    
    // For private messages between users
    @Query("SELECT m FROM Message m WHERE m.isGroupMessage = false AND ((m.sender.id = :senderId AND m.recipient.id = :recipientId) OR (m.sender.id = :recipientId AND m.recipient.id = :senderId)) ORDER BY m.timestamp")
    List<Message> findPrivateMessages(@Param("senderId") Long senderId, @Param("recipientId") Long recipientId);
    
    // For group messages
    @Query("SELECT m FROM Message m WHERE m.group.id = :groupId ORDER BY m.timestamp")
    List<Message> findByGroupId(@Param("groupId") Long groupId);
}
//public interface MessageRepository extends JpaRepository<Message, Long> {
//    // Get private messages between two users
//    @Query("SELECT m FROM Message m WHERE m.isGroupMessage = false AND " +
//           "((m.sender = :user1 AND m.recipient = :user2) OR " +
//           "(m.sender = :user2 AND m.recipient = :user1)) " +
//           "ORDER BY m.timestamp ASC")
//    List<Message> findPrivateMessages(@Param("user1") User user1, @Param("user2") User user2);
//    
//    // Get messages for a specific group
//    List<Message> findByGroupOrderByTimestampAsc(Group group);
//
//    
//    // Use proper query method naming or @Query annotation
//    @Query("SELECT m FROM Message m WHERE m.recipient IS NULL AND m.group IS NULL")
//    List<Message> findPublicMessages();
//    
//    
//}
