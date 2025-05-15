package app.chat.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import app.chat.domain.STATUS;
import app.chat.domain.USER_ROLE;
import app.chat.dto.LoginRequest;
import app.chat.dto.SignUpRequest;
import app.chat.model.User;
import app.chat.model.VerificationCode;
import app.chat.repositories.UserRepository;
import app.chat.repositories.VerificationCodeRepository;
import app.chat.response.AuthResponse;
import app.chat.utils.OtpUtils;
import app.config.impl.CustomUserServiceImpl;
import app.config.jwt.JwtService;
import lombok.RequiredArgsConstructor;

/**
 * Service for handling user authentication, including signup and sign-in with OTP verification.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationCodeRepository verificationCodeRepository;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserServiceImpl userDetailsService;

    /**
     * Creates a new user after verifying the OTP and generates a JWT token.
     *
     * @param signUpRequest Contains email, username, password, and OTP.
     * @return JWT token for the authenticated user.
     * @throws IllegalArgumentException If input is invalid or OTP is invalid/expired.
     * @throws IllegalStateException If user already exists.
     */
    public String createUser(SignUpRequest signUpRequest) {
        logger.info("Attempting to create user with email: {}", signUpRequest.getEmail());

        // Validate input
        if (signUpRequest.getEmail() == null || signUpRequest.getUsername() == null ||
                signUpRequest.getOtp() == null || signUpRequest.getOtp() == null) {
            throw new IllegalArgumentException("Email, username, password, and OTP are required");
        }

        // Verify OTP
        VerificationCode verificationCode = verificationCodeRepository
        	.findByEmail(signUpRequest.getEmail());
        if (verificationCode == null || !verificationCode.getOtp().equals(signUpRequest.getOtp())) {
            logger.warn("Invalid OTP for email: {}", signUpRequest.getEmail());
            throw new IllegalArgumentException("Invalid OTP");
        }

        // Check OTP expiration
//        if (verificationCode.getExpiry().before(new Date())) {
//            logger.warn("Expired OTP for email: {}", signUpRequest.getEmail());
//            throw new IllegalArgumentException("OTP has expired");
//        }

        // Delete OTP
       // verificationCodeRepository.delete(verificationCode);

        // Check if user already exists
//        Optional<User> existingUser = Optional.ofNullable(userRepository.findByEmail(signUpRequest.getEmail()));
//        if (existingUser.isPresent()) {
//            logger.warn("User already exists with email: {}", signUpRequest.getEmail());
//            throw new IllegalStateException("User with email " + signUpRequest.getEmail() + " already exists");
//        }

        User user = userRepository.findByEmail(signUpRequest.getEmail());
	
        if(user == null) {
        User newUser = new User();
        newUser.setEmail(signUpRequest.getEmail());
        newUser.setUsername(signUpRequest.getUsername());
        newUser.setPassword(passwordEncoder.encode(signUpRequest.getOtp()));
        newUser.setRole(USER_ROLE.USER);
        newUser.setStatus(STATUS.ACTIVE);

        User savedUser = userRepository.save(newUser);
        logger.info("User created with email: {}", savedUser.getEmail());
        }
        // Load UserDetails
        UserDetails userDetails = userDetailsService.loadUserByUsername(signUpRequest.getEmail());

        // Generate JWT token
        String token = jwtService.generateToken(userDetails);
        logger.info("JWT token generated for email: {}", signUpRequest.getEmail());
        return token;
    }

    /**
     * Sends a login OTP to the user's email.
     *
     * @param userEmail The user's email address.
     * @throws Exception 
     * @throws IllegalArgumentException If user is not found.
     */
    public void sendLoginOtp(String userEmail) throws Exception {
        logger.info("Generating login OTP for email: {}", userEmail);

        String SIGNING_PREFIX = "signin_";
        if (userEmail.startsWith(SIGNING_PREFIX)) {
            userEmail = userEmail.substring(SIGNING_PREFIX.length());
        }

        // Check if user exists
//        User user = userRepository.findByEmail(userEmail);
//        if (user == null) {
//            logger.warn("User not found with email: {}", userEmail);
//            throw new Exception("User not found");
//        }

        // Delete existing OTP
        VerificationCode existingCode = verificationCodeRepository.findByEmail(userEmail);
        if (existingCode != null) {
            verificationCodeRepository.delete(existingCode);
            logger.info("Deleted existing OTP for email: {}", userEmail);
        }

        // Generate and save new OTP
        String otp = OtpUtils.generateOtp();
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setEmail(userEmail);
        verificationCode.setOtp(otp);
       // verificationCode.setExpiry(new Date(System.currentTimeMillis() + 10 * 60 * 1000)); // 10 minutes expiry
        verificationCodeRepository.save(verificationCode);
        logger.info("OTP saved for email: {}", userEmail);

        // Send OTP via email
        String subject = "JKN Chat App Login OTP";
        String message = "Welcome to JKN Chat App Developed by JK-NANA TECH \nYour Login OTP is " + otp;
        emailService.sendVerificationOtpEmail(userEmail, otp, subject, message);
        logger.info("OTP email sent to: {}", userEmail);
    }

    /**
     * Authenticates a user using email and OTP, returning a JWT token and user details.
     *
     * @param loginRequest Contains email and OTP.
     * @return AuthResponse with JWT token and user details.
     * @throws app.chat.exception.EntityNotFoundException 
     * @throws IllegalArgumentException If OTP is invalid or expired.
     * @throws UsernameNotFoundException If user is not found.
     */
    public AuthResponse signin(LoginRequest loginRequest) throws app.chat.exception.EntityNotFoundException {
        logger.info("Attempting sign-in for email: {}", loginRequest.getEmail());

        // Validate OTP
        VerificationCode verificationCode = verificationCodeRepository.findByEmail(loginRequest.getEmail());
        if (verificationCode == null || !verificationCode.getOtp().equals(loginRequest.getOtp())) {
            logger.warn("Invalid OTP for email: {}", loginRequest.getEmail());
            throw new IllegalArgumentException("Invalid OTP");
        }

//        // Check OTP expiration
//        if (verificationCode.getExpiry().before(new Date())) {
//            logger.warn("Expired OTP for email: {}", loginRequest.getEmail());
//            throw new IllegalArgumentException("OTP has expired");
//        }

        // Delete OTP to prevent reuse
        verificationCodeRepository.delete(verificationCode);
        logger.info("Deleted OTP for email: {}", loginRequest.getEmail());

        // Load UserDetails and User entity
        User user = userService.getUserByEmail(loginRequest.getEmail());
        if (user == null) {
            logger.warn("User not found with email: {}", loginRequest.getEmail());
            throw new UsernameNotFoundException("User not found with email: " + loginRequest.getEmail());
        }

        logger.info("User status for email {}: {}", loginRequest.getEmail(), user.getStatus());

        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
            logger.info("UserDetails loaded for email {}: enabled={}", loginRequest.getEmail(), userDetails.isEnabled());
        } catch (UsernameNotFoundException e) {
            logger.warn("UserDetails not found for email: {}", loginRequest.getEmail());
            throw e;
        }

        // Create authenticated token after OTP validation
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);
        logger.info("Authentication set for email: {}", loginRequest.getEmail());

        // Generate JWT token
        String token = jwtService.generateToken(userDetails);

        // Prepare response
        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(token);
        authResponse.setMessage("Login success");
        authResponse.setUser(userService.toUserDTO(user));
        authResponse.setUserRole(user.getRole());
        logger.info("Sign-in successful for email: {}", loginRequest.getEmail());

        return authResponse;
    }
}