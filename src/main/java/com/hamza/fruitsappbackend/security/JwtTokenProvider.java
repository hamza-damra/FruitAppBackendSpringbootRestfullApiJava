package com.hamza.fruitsappbackend.security;

import com.hamza.fruitsappbackend.exception.JwtAuthenticationException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

@Component
public class JwtTokenProvider {

    private static final Logger logger = Logger.getLogger(JwtTokenProvider.class.getName());

    @Value("${app.jwt-expiration-time}")
    private long validityInMilliseconds;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    }

    // Generate token
    public String generateToken(Authentication authentication) {
        String imageUrl = "https://images.inc.com/uploaded_files/image/1920x1080/getty_481292845_77896.jpg"; // Replace with actual
        Map<String, Object> additionalClaims = Map.of("imageUrl", imageUrl); // Replace with actual claims
        String userName = authentication.getName();
        Date currentDate = new Date();
        Date expirationDate = new Date(currentDate.getTime() + validityInMilliseconds);
        JwtBuilder tokenBuilder = Jwts.builder();
        return tokenBuilder
                .setSubject(userName)
                .setIssuedAt(currentDate)
                .setExpiration(expirationDate)
                .addClaims(additionalClaims)
                .signWith(key)
                .compact();
    }

    public String getUserNameFromToken(String token) {
        token = token.trim();
        if(token.startsWith("Bearer ")){
            token = token.substring(7);
        }
        JwtParser jwtValidatorParser = Jwts.parserBuilder().setSigningKey(key).build();
        Claims claims = jwtValidatorParser.parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    // Retrieve all claims from token
    public Claims getAllClaimsFromToken(String token) {
        token = token.trim();
        if(token.startsWith("Bearer ")){
            token = token.substring(7);
        }
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Validate token
    public boolean validateToken(String token) {
        try {
            token = token.trim();
            if(token.startsWith("Bearer ")){
                token = token.substring(7);
            }
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SignatureException e) {
            logger.severe("Invalid JWT signature: " + e.getMessage());
            throw new JwtAuthenticationException("Invalid JWT token", e);
        } catch (ExpiredJwtException e) {
            logger.severe("Expired JWT token: " + e.getMessage());
            throw new JwtAuthenticationException("Expired JWT token", e);
        } catch (UnsupportedJwtException e) {
            logger.severe("Unsupported JWT token: " + e.getMessage());
            throw new JwtAuthenticationException("Unsupported JWT token", e);
        } catch (IllegalArgumentException e) {
            logger.severe("JWT claims string is empty: " + e.getMessage());
            throw new JwtAuthenticationException("JWT claims string is empty", e);
        }
    }
}
