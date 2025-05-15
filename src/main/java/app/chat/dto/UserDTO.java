package app.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private ImageDTO image;
    private boolean online;
    private String role; // Likely a string representation of USER_ROLE enum
    private String status; // Likely a string representation of STATUS enum
}