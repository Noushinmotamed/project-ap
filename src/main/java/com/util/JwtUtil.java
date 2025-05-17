package com.util;


import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class JwtUtil {

    private static final String SECRET = "bXktdXJsLXNlY3JldC1rZXktdGhhdC1pcy1iYXNlNjQtZW5jb2RlZA=="; // Example: "my-url-secret-key-that-is-base64-encoded"
    private static final Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
    private static final long EXPIRATION_MS = 3 * 60 * 60 * 1000;

    public static String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public static String validateToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        // Critical sanitization steps:
        token = token.trim()
                .replace("Bearer ", "")  // Remove auth scheme if present
                .replaceAll("\\s", "");  // Remove ALL whitespace

        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException e) {
            System.err.println("JWT Error: " + e.getClass().getSimpleName() +
                    " | Token: " + token);
            return null;
        }
    }

    public static Key getKey() {
        return key;
    }
}