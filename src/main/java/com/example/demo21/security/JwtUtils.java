package com.example.demo21.security;

import com.example.demo21.entity.PublicUserDocument;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${spring.app.jwtSecret}")
    private String jwtSecret;

    @Value("${spring.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    // Extract JWT from Authorization header
    public String getJwtFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        logger.debug("Authorization Header: {}", bearerToken);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remove Bearer prefix
        }
        return null;
    }

    // Generate token from username
    public String generateTokenFromUsername(CustomUserDetails userDetails) {
        String username = userDetails.getName();
        String email = userDetails.getEmail(); // Assuming email is available in CustomUserDetails
        logger.info("Generating JWT for username: {}", username);

        return Jwts.builder()
                .setSubject(username)
                .claim("email", email) // Add email claim
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateTokenFromOauth(PublicUserDocument userDetails) {
        String username = userDetails.getName();
        String email = userDetails.getEmail(); // Assuming email is available in PublicUserDocument
        logger.info("Generating JWT for username: {}", username);

        return Jwts.builder()
                .setSubject(username)
                .claim("email", email) // Add email claim
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }


    // Extract username from JWT token
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Validate JWT token
    public JwtValidationResult validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken);
            return new JwtValidationResult(true, "Valid token");
        } catch (MalformedJwtException e) {
            return new JwtValidationResult(false, "Invalid JWT token");
        } catch (ExpiredJwtException e) {
            return new JwtValidationResult(false, "JWT token is expired");
        } catch (UnsupportedJwtException e) {
            return new JwtValidationResult(false, "JWT token is unsupported");
        } catch (IllegalArgumentException e) {
            return new JwtValidationResult(false, "JWT claims string is empty");
        }
    }
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("email", String.class); // Ensure "email" is included in your JWT claims
    }

    // Decode and return the signing key
    private Key key() {
        try {
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid JWT secret: Ensure it is Base64-encoded.", e);
            throw e;
        }
    }
}
