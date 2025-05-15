/**
 * 
 */
package app.chat.dto;

import lombok.Data;

/**
 * @author JONATHAN
 */
@Data
public class SignUpRequest {

    private String email;
    private String username;
    private String otp;
}
