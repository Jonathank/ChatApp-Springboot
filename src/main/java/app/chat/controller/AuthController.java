/**
 * 
 */
package app.chat.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.chat.domain.USER_ROLE;
import app.chat.dto.LoginRequest;
import app.chat.dto.SignUpRequest;
import app.chat.model.VerificationCode;
import app.chat.response.ApiResponse;
import app.chat.response.AuthResponse;
import app.chat.service.AuthService;
import lombok.RequiredArgsConstructor;


/**
 * @author JONATHAN
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/KJN/chatting/app/auth")
public class AuthController {

  //  private final UserRepository userRepository;
    private final AuthService authService;
    
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse>createUserHandler(@RequestBody SignUpRequest signUpRequest) throws Exception{
	String jwt = authService.createUser(signUpRequest);
	AuthResponse authResponse = new AuthResponse();
	authResponse.setToken(jwt);
	authResponse.setMessage("User created successfully");
	authResponse.setUserRole(USER_ROLE.USER);
	
	return ResponseEntity.ok(authResponse);
    }
    
    @PostMapping("/send/signup-Otp")
    public ResponseEntity<ApiResponse>sendOtpHandler(@RequestBody VerificationCode Request) throws Exception{
	authService.sendLoginOtp(Request.getEmail());
	ApiResponse response = new ApiResponse();
	response.setMessage("Opt sent successfully");
	return ResponseEntity.ok(response);
    }
    
    @PostMapping("/signin")
    public ResponseEntity<AuthResponse>signinHandler(@RequestBody LoginRequest request) throws Exception{
	AuthResponse authResponse = authService.signin(request);
	return ResponseEntity.ok(authResponse);
    }
   
}
