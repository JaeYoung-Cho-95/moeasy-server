package com.moeasy.moeasy.service.jwt;

import com.moeasy.moeasy.config.jwt.JwtUtil;
import com.moeasy.moeasy.domain.account.RefreshToken;
import com.moeasy.moeasy.dto.account.KaKaoDto;
import com.moeasy.moeasy.repository.account.RefreshTokenRepository;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private JwtUtil jwtUtil;
  private RefreshTokenRepository refreshTokenRepository;

  /**
   * user email 을 기반으로 accessToken, refreshToken 을 생성 refreshToken db 에 email 과 매칭되게 저장
   */
  public List<String> getTokens(KaKaoDto kakaoInfo) {
    final String accessToken = jwtUtil.generateAccessToken(kakaoInfo.getEmail());
    final String refreshToken = jwtUtil.generateRefreshToken(kakaoInfo.getEmail());

    refreshTokenRepository.findByUserEmail(kakaoInfo.getEmail())
        .ifPresentOrElse(
            token -> token.updateToken(refreshToken),
            () -> refreshTokenRepository.save(new RefreshToken(kakaoInfo.getEmail(), refreshToken))
        );

    return Arrays.asList(accessToken, refreshToken);
  }

  /**
   * refreshToken 을 response cookie 안에 담아서 반환
   */
  public ResponseCookie makeResponseCookie(String refreshToken) {
    return ResponseCookie.from("refresh_token", refreshToken)
        .httpOnly(true)
        .secure(true)
        .path("/")
        .maxAge(Duration.ofDays(7))
        .sameSite("Lax")
        .build();
  }
}
