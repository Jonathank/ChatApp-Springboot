// src/main/java/app/chat/model/Image.java
package app.chat.model;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Blob;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents an image associated with a user in the chat application.
 * Stores the image as a BLOB and a download URL for frontend access.
 *
 * @author Jonathan
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

   
    @Lob
    @Column(name = "image")
    private byte[] image;

    private String downloadUrl;

    @OneToOne(mappedBy = "image")
    private User user;

    @OneToOne(mappedBy = "image")
    private Group group;
}