package com.moeasy.moeasy.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

  @Value("${jwt.secret-key}")
  private String secret;

  private Key secretKey;

  private static final long ACCESS_TOKEN_EXPIRATION_TIME = 1000 * 60 * 120; // 2시간
  private static final long REFRESH_TOKEN_EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 7; // 7일

//    private static final long ACCESS_TOKEN_EXPIRATION_TIME = 1000 * 30; // 1분
//    private static final long REFRESH_TOKEN_EXPIRATION_TIME = 1000 * 60; // 7일

  @PostConstruct
  public void init() {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
  }

  public String generateAccessToken(String email) {
    Map<String, Object> claims = new HashMap<>();
    return createToken(claims, email, ACCESS_TOKEN_EXPIRATION_TIME);
  }

  public String generateRefreshToken(String email) {
    Map<String, Object> claims = new HashMap<>();
    return createToken(claims, email, REFRESH_TOKEN_EXPIRATION_TIME);
  }

  private String createToken(Map<String, Object> claims, String subject, long expirationTime) {
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(subject)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
        .signWith(secretKey, SignatureAlgorithm.HS256)
        .compact();
  }

  public Boolean validateToken(String token, String email) {
    final String extractedEmail = extractEmail(token);
    return (extractedEmail.equals(email) && !isTokenExpired(token));
  }

  public String extractEmail(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
  }

  private Boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }
}
