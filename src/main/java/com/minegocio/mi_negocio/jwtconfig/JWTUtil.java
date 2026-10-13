package com.minegocio.mi_negocio.jwtconfig;

import io.jsonwebtoken.Claims; 
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JWTUtil {

    @Value("${jwt.secret.key}")
    private String secretKey;

    @Value("${jwt.time.expiration}")
    private long expirationTime; // milisegundos

    private Key getKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length < 64) {
            throw new IllegalArgumentException("JWT secret key must be at least 64 bytes for HS512");
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    // subject = email (el login es por email)
    public String generateToken(String email, String platformRole, String userId) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", platformRole)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // Lanza JwtException si el token es inválido o está vencido
    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token.replace("Bearer ", ""))
                .getBody();
    }

    public String getEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public String getRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    public UUID getUserId(String token) {
        return UUID.fromString(extractClaims(token).get("userId", String.class));
    }

    public boolean validateToken(String token) {
        try {
            extractClaims(token); // el parser ya valida firma y expiración
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
