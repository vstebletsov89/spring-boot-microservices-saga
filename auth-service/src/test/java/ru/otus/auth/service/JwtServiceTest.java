package ru.otus.auth.service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        String testSecret = "14e2fd772ac68832dfe104a5c87a9055";
        ReflectionTestUtils.setField(jwtService, "secret", testSecret);
        jwtService.init();
    }

    @Test
    void generateAccessToken_ShouldReturnValidToken() {
        String token = jwtService.generateAccessToken("testUser");

        System.out.println(token);
        assertThat(token).isNotBlank();
        assertTrue(jwtService.isValid(token));
    }

    @Test
    void generateRefreshToken_ShouldReturnValidToken() {
        String token = jwtService.generateRefreshToken("testUser");

        System.out.println(token);
        assertThat(token).isNotBlank();
        assertTrue(jwtService.isValid(token));
    }

    @Test
    void isValid_ShouldReturnFalseForInvalidToken() {
        String invalidToken = "abc.def.ghi";

        assertFalse(jwtService.isValid(invalidToken));
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        String username = "extractUser";
        String token = jwtService.generateAccessToken(username);

        assertEquals(username, jwtService.extractUsername(token));
    }

    @Test
    void extractUsername_ShouldThrowForMalformedToken() {
        String invalidToken = "not.valid.token";

        assertThrows(JwtException.class, () -> jwtService.extractUsername(invalidToken));
    }

    @Test
    void isValid_ShouldReturnFalseForExpiredToken() {
        String username = "expiredUser";
        Key key = (Key) ReflectionTestUtils.getField(jwtService, JwtService.class, "key");

        String expiredToken = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new java.util.Date(System.currentTimeMillis() - 2000))
                .setExpiration(new java.util.Date(System.currentTimeMillis() - 1000))
                .signWith(key)
                .compact();

        assertFalse(jwtService.isValid(expiredToken));
    }
}