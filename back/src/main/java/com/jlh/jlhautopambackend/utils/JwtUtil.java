package com.jlh.jlhautopambackend.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretBase64;

    /** Durée de validité du token en millisecondes. */
    @Value("${jwt.expiration}")
    private long expirationMs;

    private Key signingKey;

    /**
     * Initialise la clé de signature une seule fois.
     */
    @PostConstruct
    private void initSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretBase64);
        signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Génère un JWT à partir du nom d’utilisateur et son role.
     */
    public String generateToken(UserDetails userDetails) {
        Instant now = Instant.now();
        Instant expiry = now.plus(Duration.ofMillis(expirationMs));

        // On récupère les authorities (rôles) pour le token
        var authorities = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("authorities", authorities)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Valide la signature et la date d’expiration du token.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // signature invalide, token expiré, etc.
            return false;
        }
    }

    /**
     * Extrait le nom d’utilisateur (sub) du JWT.
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
}
