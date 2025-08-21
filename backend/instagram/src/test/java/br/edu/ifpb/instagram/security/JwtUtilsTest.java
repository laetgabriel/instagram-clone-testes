package br.edu.ifpb.instagram.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("Deve gerar um token JWT válido e não vazio")
    void testGenerateToken_ShouldReturnNonEmptyToken() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("testuser");

        String token = jwtUtils.generateToken(auth);

        assertNotNull(token, "Token não deve ser nulo");
        assertFalse(token.isBlank(), "Token não deve estar vazio");
    }

    @Test
    @DisplayName("Deve validar corretamente um token JWT válido")
    void testValidateToken_ShouldReturnTrueForValidToken() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("testuser");

        String token = jwtUtils.generateToken(auth);

        assertTrue(jwtUtils.validateToken(token), "Token deve ser válido");
    }

    @Test
    @DisplayName("Deve retornar falso para um token JWT malformado")
    void testValidateToken_ShouldReturnFalseForMalformedToken() {
        String invalidToken = "esse token .é.. invalido.";

        assertFalse(jwtUtils.validateToken(invalidToken), "Token inválido deve ser rejeitado");
    }

    @Test
    @DisplayName("Deve retornar falso para um token JWT expirado")
    void testValidateToken_ShouldReturnFalseForExpiredToken() {
        String expiredToken = io.jsonwebtoken.Jwts.builder()
                .setSubject("expireduser")
                .setIssuedAt(new Date(System.currentTimeMillis() - 2000))
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(Keys.hmacShaKeyFor(Base64.getEncoder()
                        .encode("umaChaveMuitoSeguraDePeloMenos64CaracteresParaHS512JwtAlgoritmo".getBytes())),
                        SignatureAlgorithm.HS512)
                .compact();

        assertFalse(jwtUtils.validateToken(expiredToken), "Token expirado deve ser rejeitado");
    }

    @Test
    @DisplayName("Deve extrair corretamente o nome de usuário de um token JWT válido")
    void testGetUsernameFromToken_ShouldReturnCorrectUsername() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("testuser");

        String token = jwtUtils.generateToken(auth);
        String username = jwtUtils.getUsernameFromToken(token);

        assertEquals("testuser", username, "Usuário deve ser o mesmo contido no token");
    }
}
