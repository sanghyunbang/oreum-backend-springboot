package com.oreum.auth.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Random;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@Component
public class JWTUtil {

    private final SecretKey secretKey;

    public JWTUtil(@Value("${spring.jwt.secret}") String secret) {
        System.out.println("Initializing JWTUtil with secret key.");
        this.secretKey = new SecretKeySpec(
            secret.getBytes(StandardCharsets.UTF_8),
            "HmacSHA256"
        );
    }

    public String createJwt(String username, String nickname, String role, Long expiredMs) {
        String jwt = Jwts.builder()
            .claim("username", username)
            .claim("nickname", nickname)
            .claim("role", role)
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + expiredMs))
            .signWith(secretKey)
            .compact();
        System.out.println("[O] JWT created for: " + username +", username: "+ nickname + ", role: " + role + ", expires in ms: " + expiredMs);
        return jwt;
    }

    public String getUsername(String token) {
        try {
            String username = parseClaims(token).get("username", String.class);
            System.out.println("Extracted username: " + username);
            return username;
        } catch (Exception e) {
            System.out.println("[X] Failed to extract username: " + e.getMessage());
            throw new RuntimeException("Invalid or expired JWT (username)", e);
        }
    }

    public String getRole(String token) {
        try {
            String role = parseClaims(token).get("role", String.class);
            System.out.println("Extracted role: " + role);
            return role;
        } catch (Exception e) {
            System.out.println("[X] Failed to extract role: " + e.getMessage());
            throw new RuntimeException("Invalid or expired JWT (role)", e);
        }
    }

    public Boolean isExpired(String token) {
        try {
            Date exp = parseClaims(token).getExpiration();
            boolean expired = exp.before(new Date());
            System.out.println("[!] Token expiration check: " + expired + " (exp=" + exp + ")");
            return expired;
        } catch (Exception e) {
            System.out.println("[X] Expiration check failed: " + e.getMessage());
            throw new RuntimeException("Invalid or expired JWT (expiration)", e);
        }
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (Exception e) {
            System.out.println("[X] Failed to parse claims: " + e.getMessage());
            throw e;
        }
    }

}
