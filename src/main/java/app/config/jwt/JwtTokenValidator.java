package app.config.jwt;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import app.config.impl.CustomUserServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtTokenValidator extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final CustomUserServiceImpl customUserServiceImpl;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
//	String uri = request.getRequestURI();
//        // Skip JWT validation for auth and CSRF endpoints
//        if (uri.startsWith("/KJN/chatting/app/auth/") || uri.equals("/csrf") || uri.startsWith("/ws/")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
	 // Skip filter for OPTIONS requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }
        String authHeader = request.getHeader("Authorization");
        String requestURI = request.getRequestURI();
        System.out.println("HTTP Request URI: " + requestURI + ", Authorization Header: " + authHeader);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            System.out.println("HTTP JWT Token: " + token);
            try {
                String email = jwtService.getEmailFromToken(token);
                System.out.println("HTTP Extracted Email: " + email);
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = customUserServiceImpl.loadUserByUsername(email);
                    if (userDetails == null) {
                        System.err.println("HTTP UserDetails not found for email: " + email);
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found for email: " + email);
                        return;
                    }
                    System.out.println("HTTP UserDetails: " + userDetails.getUsername() + ", Authorities: " + userDetails.getAuthorities());
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    System.out.println("HTTP Authentication Principal: " + authToken.getPrincipal().getClass().getName());
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                System.err.println("HTTP JWT Validation Error: " + e.getMessage());
                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT Validation failed: " + e.getMessage());
                return;
            }
        } else {
            System.out.println("HTTP No Authorization Header or Invalid Format for URI: " + requestURI + ", Header: " + authHeader);
        }
        filterChain.doFilter(request, response);
    }
}