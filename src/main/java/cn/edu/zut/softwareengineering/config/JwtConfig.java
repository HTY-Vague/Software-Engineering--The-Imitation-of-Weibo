package cn.edu.zut.softwareengineering.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret:software-engineering-secret-key-for-jwt-token-generation-2024}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    public SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Long getExpiration() {
        return expiration;
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Date getExpirationDate() {
        return new Date(System.currentTimeMillis() + expiration);
    }
}
