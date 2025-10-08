package com.goodleap.healthtracker.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

public class TokenFactory {
    private static final String DEFAULT_SECRET =
            "super-secret-demo-key-please-rotate-change-in-prod-1234567890";

    private static final String CONFIGURED_SECRET = System.getenv()
            .getOrDefault("JWT_SIGNING_SECRET", DEFAULT_SECRET);

    private static final SecretKey KEY =
            Keys.hmacShaKeyFor(CONFIGURED_SECRET.getBytes(StandardCharsets.UTF_8));

    public static JwtParser parser() {
        return Jwts.parserBuilder().setSigningKey(KEY).build();
    }

    public static String sampleAdminToken() {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject("admin-user")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(3600)))
                .addClaims(Map.of("isAdmin", true))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }
}
