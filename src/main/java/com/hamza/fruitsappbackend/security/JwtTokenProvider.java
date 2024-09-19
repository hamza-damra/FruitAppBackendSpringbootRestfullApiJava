package com.hamza.fruitsappbackend.security;

import com.hamza.fruitsappbackend.modulus.user.entity.User;
import com.hamza.fruitsappbackend.security.exception.JwtAuthenticationException;
import com.hamza.fruitsappbackend.modulus.user.exception.UserNotFoundException;
import com.hamza.fruitsappbackend.modulus.user.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Logger;

@Component
public class JwtTokenProvider {

    private static final Logger logger = Logger.getLogger(JwtTokenProvider.class.getName());

    @Value("${app.jwt-expiration-time}")
    private long validityInMilliseconds;

    private Key key;

    private final UserRepository userRepository;

    @Autowired
    public JwtTokenProvider(UserRepository userRepository) {
        this.userRepository = userRepository;

    }

    @PostConstruct
    public void init() {
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    }


    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        String email = userPrincipal.getUsername();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("email", email));

        String userId = user.getId().toString();
        Boolean isVerified = user.getIsVerified();
        String imageUrl = user.getImageUrl() == null ? "" : user.getImageUrl();
        String name = user.getName();

        Date currentDate = new Date();
        Date expirationDate = new Date(currentDate.getTime() + validityInMilliseconds);

        String formattedIssuedAt = formatDate(currentDate);
        String formattedExpiration = formatDate(expirationDate);

        Map<String, Object> additionalClaims = Map.of(
                "userId", userId,
                "name" , name,
                "imageUrl", imageUrl,
                "isVerified", isVerified,
                "iat", formattedIssuedAt,
                "exp", formattedExpiration
        );

        String token = Jwts.builder()
                .setSubject(email)
                .setIssuedAt(currentDate)
                .setExpiration(expirationDate)
                .addClaims(additionalClaims)
                .signWith(key)
                .compact();

        return token;
    }

    private String formatDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }


    public String getUserNameFromToken(String token) {
        token = token.trim();
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        JwtParser jwtValidatorParser = Jwts.parserBuilder().setSigningKey(key).build();
        Claims claims = jwtValidatorParser.parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    public Claims getAllClaimsFromToken(String token) {
        token = token.trim();
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            token = token.trim();
            if (token.startsWith("Bearer ")) {
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


    public String getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("userId", String.class);
    }
}
