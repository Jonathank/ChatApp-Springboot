/**
 * 
 */
package app.chat.response;

import app.chat.domain.USER_ROLE;
import app.chat.dto.UserDTO;
import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author JONATHAN
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String message;
    private USER_ROLE userRole;
    private UserDTO user;
}
