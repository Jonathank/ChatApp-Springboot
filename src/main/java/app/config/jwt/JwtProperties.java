package app.config.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
@ConfigurationProperties(prefix = "spring.auth.token")
public class JwtProperties {
    private long expiration;
    private String jwtSecret;
}
