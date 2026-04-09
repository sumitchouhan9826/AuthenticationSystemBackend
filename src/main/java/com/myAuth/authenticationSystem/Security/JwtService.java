package com.myAuth.authenticationSystem.Security;



import com.myAuth.authenticationSystem.entities.Role;
import com.myAuth.authenticationSystem.entities.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Getter
@Setter
public class JwtService {
    private final SecretKey key;
    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;
    private final String issuer;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.access-ttl-seconds}") long accessTtlSeconds,
            @Value("${security.jwt.refresh-ttl-seconds}")   long refreshTtlSeconds,
            @Value("${security.jwt.issuer}") String issuer) {

        if(secret==null || secret.length()<64) {
            throw new IllegalArgumentException("Invalid secret");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTtlSeconds = accessTtlSeconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
        this.issuer = issuer;
    }

    // generate token
    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        List<String> roles = user.getRoles() == null? List.of() :
                user.getRoles().stream().map(Role::getName)
                        .toList();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTtlSeconds)))
                .claims(Map.of(
                        "email", user.getEmail(),
                        "roles", roles,
                        "type", "access"
                ))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

    }

    public String generateRefreshToken(User user, String jti) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(jti)
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshTtlSeconds)))
                .claims(Map.of(
                        "email", user.getEmail(),
                        "type", "refresh"
                ))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

    }

    //parse the token
    public Jws<Claims> parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
    }

    public boolean isAccessToken(String token){
            Claims claims = parse(token).getPayload();
            return "access".equals(claims.get("type"));

    }
        public boolean isRefreshToken(String token){
            Claims claims = parse(token).getPayload();
            return "refresh".equals(claims.get("type"));

        }
        public UUID getUserId(String token){
            Claims claims = parse(token).getPayload();
            return UUID.fromString(claims.getSubject());
        }
        public String getJti(String token){
            Claims claims = parse(token).getPayload();
            return claims.getId();
        }
        public List<String> getRoles(String token){
            Claims claims = parse(token).getPayload();
            return (List<String>) claims.get("roles");
        }
        public String getEmail(String token){
            Claims claims = parse(token).getPayload();
            return (String) claims.get("email");
        }

}
