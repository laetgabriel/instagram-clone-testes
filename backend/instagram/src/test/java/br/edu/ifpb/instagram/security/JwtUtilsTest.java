package br.edu.ifpb.instagram.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.util.Base64;
import java.util.Date;

public class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
    }

    @Test
    void testGenerateToken_ShouldReturnNonEmptyToken() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("testuser");

        String token = jwtUtils.generateToken(auth);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void testValidateToken_ShouldReturnTrueForValidToken() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("testuser");

        String token = jwtUtils.generateToken(auth);

        assertTrue(jwtUtils.validateToken(token));
    }

    @Test
    void testValidateToken_ShouldReturnFalseForMalformedToken() {
        String invalidToken = "esse token .é.. invalido.";

        assertFalse(jwtUtils.validateToken(invalidToken));
    }

    @Test
    void testValidateToken_ShouldReturnFalseForExpiredToken() {
        String expiredToken = io.jsonwebtoken.Jwts.builder()
                .setSubject("expireduser")
                .setIssuedAt(new Date(System.currentTimeMillis() - 2000))
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(Keys.hmacShaKeyFor(Base64.getEncoder()
                        .encode("umaChaveMuitoSeguraDePeloMenos64CaracteresParaHS512JwtAlgoritmo".getBytes())), 
                        SignatureAlgorithm.HS512)
                .compact();

        assertFalse(jwtUtils.validateToken(expiredToken));
    }

    @Test
    void testGetUsernameFromToken_ShouldReturnCorrectUsername() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("testuser");

        String token = jwtUtils.generateToken(auth);

        String username = jwtUtils.getUsernameFromToken(token);

        assertEquals("testuser", username);
    }
}
