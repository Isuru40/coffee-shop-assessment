package com.coffeeshop.auth_service.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String secretKey = "testsecure32charsecretkey12345678";
    private final long expiration = 86400000;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "SECRET_KEY", secretKey);
        ReflectionTestUtils.setField(jwtUtil, "ACCESS_TOKEN_EXPIRATION", expiration);
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        String username = "owner1";
        String token = jwtUtil.generateToken(username);

        assertNotNull(token);
        Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
        assertEquals(username, claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void getUsernameFromToken_ShouldReturnCorrectUsername() {
        String username = "owner1";
        String token = jwtUtil.generateToken(username);

        String extractedUsername = jwtUtil.getUsernameFromToken(token);
        assertEquals(username, extractedUsername);
    }

    @Test
    void validateToken_ValidToken_ShouldReturnTrue() {
        String username = "owner1";
        String token = jwtUtil.generateToken(username);

        boolean isValid = jwtUtil.validateToken(token);
        assertTrue(isValid);
    }

    @Test
    void validateToken_InvalidToken_ShouldReturnFalse() {
        String invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid.payload";
        boolean isValid = jwtUtil.validateToken(invalidToken);
        assertFalse(isValid);
    }

    @Test
    void validateToken_ExpiredToken_ShouldReturnFalse() {
        String username = "owner1";
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis() - 2 * expiration))
                .setExpiration(new Date(System.currentTimeMillis() - expiration))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        boolean isValid = jwtUtil.validateToken(token);
        assertFalse(isValid);
    }
}