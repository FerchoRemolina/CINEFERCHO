package com.cinefercho.security;

import com.cinefercho.entity.User;
import com.cinefercho.entity.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public Instant issueExpiresAt(UserRole role) {
        long ttl = role == UserRole.ROLE_CLIENT
                ? jwtProperties.clientExpirationMs()
                : jwtProperties.expirationMs();
        return Instant.now().plusMillis(ttl);
    }

    public String generateToken(UserPrincipal principal, Instant expiresAt) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(principal.getUsername())
                .claim("uid", principal.getId())
                .claim("fullName", principal.getFullName())
                .claim("role", principal.getRole().name())
                .claim("membershipType", principal.getMembershipType().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey())
                .compact();
    }

    public String generateToken(User user, Instant expiresAt) {
        return generateToken(UserPrincipal.from(user), expiresAt);
    }

    public String generateToken(UserPrincipal principal) {
        return generateToken(principal, issueExpiresAt(principal.getRole()));
    }

    public String generateToken(User user) {
        return generateToken(user, issueExpiresAt(user.getRole()));
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }
}
