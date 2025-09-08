package com.moeasy.moeasy.config.jwt;

import com.moeasy.moeasy.config.response.custom.CustomErrorException;
import com.moeasy.moeasy.domain.account.RefreshToken;
import com.moeasy.moeasy.dto.account.request.RefreshTokenForAppDto;
import com.moeasy.moeasy.dto.account.response.RefreshTokensDto;
import com.moeasy.moeasy.repository.account.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtUtil {

  @Value("${jwt.secret-key}")
  private String secret;

  private Key secretKey;

  private final RefreshTokenRepository refreshTokenRepository;
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

  public RefreshTokensDto getRefreshToken(HttpServletRequest request, RefreshTokenForAppDto dto) {
    String accessToken = getAccessToken(request);

    // 2) Access Token 유효성 검사
    String userEmail = extractEmailAllowExpired(accessToken);
    boolean isAccessTokenExpired = isAccessTokenExpired(accessToken);

    String refreshToken = extractedRefreshToken(request, dto);

    // 4) Access Token이 아직 유효하면 그대로 반환
    if (!isAccessTokenExpired) {
      return RefreshTokensDto.from(accessToken, refreshToken);
    }

    // 5) DB에서 Refresh Token 조회 및 일치/유효성 + refreshToken 만료 시간 확인
    RefreshToken storedRefreshToken = validateRefreshToken(refreshToken, userEmail);

    // 6) 새 토큰 발급 + DB 업데이트
    String newAccessToken = generateAccessToken(userEmail);
    String newRefreshToken = generateRefreshToken(userEmail);

    storedRefreshToken.updateToken(newRefreshToken);
    refreshTokenRepository.save(storedRefreshToken);

    return RefreshTokensDto.from(newAccessToken, newRefreshToken);
  }

  private String extractedRefreshToken(HttpServletRequest request, RefreshTokenForAppDto dto) {
    if (dto != null) {
      String refreshToken = dto.getRefreshToken();
      if (refreshToken != null && !refreshToken.isEmpty()) {
        return refreshToken;
      } else {
        throw CustomErrorException.from(HttpStatus.BAD_REQUEST,
            "refreshToken 이  Body 안에 존재하지 않습니다.");
      }
    } else {
      return getRefreshTokenFromCookies(request);
    }
  }

  private String getRefreshTokenFromCookies(HttpServletRequest request) {
    if (request.getCookies() != null) {
      for (jakarta.servlet.http.Cookie c : request.getCookies()) {
        if ("refresh_token".equals(c.getName())) {
          return validateRefreshTokenFromCookies(c.getValue());
        }
      }
    }
    throw CustomErrorException.from(HttpStatus.BAD_REQUEST, "Cookie 에서 refreshToken 이 조회되지 않았습니다.");
  }

  private String validateRefreshTokenFromCookies(String refreshToken) {
    if (refreshToken == null || refreshToken.isEmpty()) {
      throw CustomErrorException.from(HttpStatus.UNAUTHORIZED,
          "Cookie 에서 refreshToken 이 조회되지 않았습니다.");
    }
    return refreshToken;
  }

  @NotNull
  private static String getAccessToken(HttpServletRequest request) {
    String authorization = request.getHeader("Authorization");
    if (authorization == null || !authorization.startsWith("Bearer ")) {
      throw CustomErrorException.from(HttpStatus.BAD_REQUEST, "header 에 유효한 AccessToken 이 없습니다.");
    }

    String accessToken = authorization.substring(7);
    return accessToken;
  }

  private String extractEmailAllowExpired(String token) {
    try {
      return extractEmail(token);
    } catch (ExpiredJwtException e) {
      if (e.getClaims() == null || e.getClaims().getSubject() == null) {
        throw CustomErrorException.from(HttpStatus.UNAUTHORIZED, "Access Token 이 유효하지 않습니다.");
      }
      return e.getClaims().getSubject();
    } catch (Exception e) {
      throw CustomErrorException.from(HttpStatus.UNAUTHORIZED, "Access Token 이 유효하지 않습니다.");
    }
  }

  private boolean isAccessTokenExpired(String token) {
    try {
      Date exp = extractExpiration(token); // 만료면 여기서 ExpiredJwtException
      return exp.before(new Date());
    } catch (ExpiredJwtException e) {
      return true;
    }
  }

  private RefreshToken validateRefreshToken(String refreshToken, String userEmail) {
    RefreshToken storedToken = refreshTokenRepository.findByUserEmail(userEmail).orElse(null);
    if (storedToken == null || !storedToken.getToken().equals(refreshToken)) {
      throw CustomErrorException.from(HttpStatus.UNAUTHORIZED, "refreshToken이 이메일과 일치하지 않습니다.");
    }

    return validateExpiredRefreshToken(storedToken, userEmail);
  }

  private RefreshToken validateExpiredRefreshToken(RefreshToken refreshToken, String userEmail) {
    try {
      validateToken(refreshToken.getToken(), userEmail);
      return refreshToken;
    } catch (Exception e) {
      refreshTokenRepository.delete(refreshToken);
      throw CustomErrorException.from(HttpStatus.UNAUTHORIZED,
          "Refresh Token이 만료되었습니다. 다시 로그인해주세요.");
    }
  }

  public void deleteRefreshToken(HttpServletRequest request) {
    String accessToken = request.getHeader("Authorization").substring(7);
    String userEmail = extractEmail(accessToken);
    refreshTokenRepository.findByUserEmail(userEmail).ifPresent(refreshTokenRepository::delete);
  }
}
