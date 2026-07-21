package com.tutorapp.tutorapp.security;

import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    // VALORES POR DEFECTO DIRECTAMENTE EN EL CÓDIGO
    private static final String DEFAULT_SECRET = "5X8nE2rT9vW4yK7mQ3pJ6sH1cF0zL9xG";
    private static final long DEFAULT_EXPIRATION = 3600000;

    // Spring intentará cargar del properties, si no, usa el valor por defecto
    @Value("${jwt.secret:" + DEFAULT_SECRET + "}")
    private String secret;

    @Value("${jwt.expiration:" + DEFAULT_EXPIRATION + "}")
    private long expirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public String generarToken(String username, String rol, Long id, String nombre) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expirationMs);

        return Jwts.builder()
                .subject(username)
                .claim("rol", rol)
                .claim("id", id)
                .claim("nombre", nombre)
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(getSigningKey())
                .compact();
    }

    public String extraerUsername(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    public String extraerRol(String token) {
        return extraerTodosLosClaims(token).get("rol", String.class);
    }

    public Long extraerId(String token) {
        return extraerTodosLosClaims(token).get("id", Long.class);
    }

    public String extraerNombre(String token) {
        return extraerTodosLosClaims(token).get("nombre", String.class);
    }

    public Date extraerExpiracion(String token) {
        return extraerClaim(token, Claims::getExpiration);
    }

    private <T> T extraerClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extraerTodosLosClaims(token);
        return resolver.apply(claims);
    }

    private Claims extraerTodosLosClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean esTokenValido(String token, String username) {
        try {
            String tokenUsername = extraerUsername(token);
            return tokenUsername.equals(username) && !esTokenExpirado(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean esTokenExpirado(String token) {
        return extraerExpiracion(token).before(new Date());
    }
}