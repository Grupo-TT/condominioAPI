package com.condominio.util.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static com.condominio.util.constants.AppConstants.*;

@Component
public class JwtUtil {
    private final Key key;
    private final long accessTokenValidityMillis;
    private final long refreshTokenValidityMillis;


    public JwtUtil(JwtProperties jwtProperties) {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
        this.accessTokenValidityMillis = jwtProperties.getExpiration();
        this.refreshTokenValidityMillis = jwtProperties.getRefreshExpiration();
    }

    public String generateAccessToken(UserDetails user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenValidityMillis);


        List<String> rol = user.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .toList();

        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", rol);
        claims.put("type", "access");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuer("condominioAPI")
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(UserDetails user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenValidityMillis);

        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");


        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuer("condominioAPI")
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {

            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            String type = (String) claims.get("type");
            return "refresh".equals(type);
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUsername(String token) {return extractAllClaims(token).getSubject();}

    public String extractRole(String token) {
        return extractAllClaims(token).get("rol", String.class);
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public long getAccessTokenValiditySeconds() {
        return accessTokenValidityMillis / 1000;
    }

    public long getRefreshTokenValiditySeconds() {
        return refreshTokenValidityMillis / 1000;
    }

    private String formatDate(Date date) {
        if (date == null) return "sin expiración";
        Instant instant = date.toInstant();
        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZONE);
        return zdt.format(READABLE_FORMATTER); // Ej: "2025-10-09 14:30:00 -05"
    }

    public String getExpirationReadable(String token) {
        try {
            Date exp = extractExpiration(token);
            return formatDate(exp);
        } catch (Exception e) {
            return "token inválido";
        }
    }

    public String getIssuedAtReadable(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Date iat = claims.getIssuedAt();
            return formatDate(iat);
        } catch (Exception e) {
            return "token inválido";
        }
    }

    public String getAccessTokenExpiryFromNowReadable() {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenValidityMillis);
        return formatDate(expiry);
    }

    public String getRefreshTokenExpiryFromNowReadable() {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenValidityMillis);
        return formatDate(expiry);
    }


}

