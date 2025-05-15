package app.chat.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import app.chat.model.Group;

public interface GroupRepository extends JpaRepository<Group, Long>{
    Optional<Group> findByGroupname(String groupname);

    @Query("SELECT g FROM Group g JOIN FETCH g.members m WHERE m.id = :userId")
    List<Group> findByMembersId(Long userId);

    @Query("SELECT g FROM Group g LEFT JOIN FETCH g.groupAdmins WHERE g.id = :id")
    Optional<Group> findByIdWithAdmins(Long id);

}
