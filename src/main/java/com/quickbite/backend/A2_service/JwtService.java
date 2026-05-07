package com.quickbite.backend.A2_service;

import com.quickbite.backend.model.AppUser;
import com.quickbite.backend.principal.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    @Value("${secret.key}")
    private String SECRET_KEY;

    public SecretKey getKey(){
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public Claims extractAllClaims(String token){
        return Jwts
                .parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token) // sign, expiry
                .getPayload();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String generateToken(UserPrincipal user, Date expiry) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole());
        claims.put("email", user.getUsername()); // gets email !

        return Jwts
                .builder()
                .claims(claims)
                .subject(String.valueOf(user.getUserId()))
                .issuedAt(new Date())
                .expiration(expiry) // 24hrs
                .signWith(getKey())
                .compact();
    }
}
