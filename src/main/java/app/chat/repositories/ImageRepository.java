/**
 * 
 */
package app.chat.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import app.chat.model.Image;

/**
 * @author JONATHAN
 */
public interface ImageRepository extends JpaRepository<Image,Long> {


    Image getImageByUserId(Long userId);

    Optional<Image> findByUserId(Long userId);

 

}
