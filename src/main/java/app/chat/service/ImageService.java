package app.chat.service;

import app.chat.dto.ImageDTO;
import app.chat.exception.EntityNotFoundException;
import app.chat.model.Group;
import app.chat.model.Image;
import app.chat.model.ImageOwner;
import app.chat.model.User;
import app.chat.repositories.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {
    private final ImageRepository imageRepository;

    @Transactional
    public ImageDTO saveImage(ImageOwner owner, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file cannot be empty");
        }
        if (owner == null) {
            throw new IllegalArgumentException("Owner cannot be null");
        }

        Image image = new Image();
        try {
            // Set byte[] directly instead of using SerialBlob
            image.setImage(file.getBytes());
            
            if (owner instanceof User) {
                image.setUser((User) owner);
            } else if (owner instanceof Group) {
                image.setGroup((Group) owner);
            }
            
            Image savedImage = imageRepository.save(image);
            savedImage.setDownloadUrl("/KJN/chatting/app/users/image/download/" + savedImage.getId());
            Image updatedImage = imageRepository.save(savedImage);
            return toImageDTO(updatedImage);
        } catch (Exception e) {
            log.error("Failed to save image for owner: {}", owner.getId(), e);
            throw new IOException("Failed to save image", e);
        }
    }

    @Transactional
    public ImageDTO updateImage(Long imageId, MultipartFile file) throws IOException, EntityNotFoundException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file cannot be empty");
        }

        Image image = getImageById(imageId);
        try {
            // Set byte[] directly instead of using SerialBlob
            image.setImage(file.getBytes());
            
            image.setDownloadUrl("/KJN/chatting/app/users/image/download/" + imageId);
            Image updatedImage = imageRepository.save(image);
            return toImageDTO(updatedImage);
        } catch (Exception e) {
            log.error("Failed to update image with id: {}", imageId, e);
            throw new IOException("Failed to update image", e);
        }
    }

    @Transactional
    public void deleteImage(Long imageId) {
        imageRepository.findById(imageId).ifPresentOrElse(
                imageRepository::delete,
                () -> 
                    new EntityNotFoundException("Image not found with id: " + imageId)
                
        );
    }

    @Transactional(readOnly = true)
    public Image getImageById(Long imageId) throws EntityNotFoundException {
        return imageRepository.findById(imageId)
                .orElseThrow(() -> new EntityNotFoundException("Image not found with id: " + imageId));
    }

    private ImageDTO toImageDTO(Image image) {
        return new ImageDTO(image.getId(), image.getDownloadUrl());
    }
}