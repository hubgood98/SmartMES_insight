package com.smartfactory.smartmes_insight.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long tokenValidityInMillis;

    private Key key;

    @PostConstruct
    protected void init() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // 토큰 생성
    public String createToken(String username, String role) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("role", role);

        Date now = new Date();
        Date expiry = new Date(now.getTime() + tokenValidityInMillis);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰에서 사용자명 추출
    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    // 토큰에서 역할(Role) 추출
    public String getRole(String token) {
        return (String) parseClaims(token).get("role");
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Claims 파싱 (만료 포함)
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    // 토큰에서 Authentication 객체 생성
    public Authentication getAuthentication(String token) {
        String username = getUsername(token);
        String role = getRole(token);
        
        // Spring Security의 권한은 "ROLE_" 접두사가 필요
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
        
        // Authentication 객체 생성
        // principal: 사용자명, credentials: null (이미 인증됨), authorities: 권한 목록
        return new UsernamePasswordAuthenticationToken(
                username,
                null,
                Collections.singletonList(authority)
        );
    }
}