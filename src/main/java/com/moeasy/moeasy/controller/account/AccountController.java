package com.moeasy.moeasy.controller.account;

import com.moeasy.moeasy.common.FailApiResponseDto;
import com.moeasy.moeasy.common.SuccessApiResponseDto;
import com.moeasy.moeasy.domain.account.RefreshToken;
import com.moeasy.moeasy.dto.account.KaKaoDto;
import com.moeasy.moeasy.dto.account.RefreshDto;
import com.moeasy.moeasy.repository.account.RefreshTokenRepository;
import com.moeasy.moeasy.service.account.KakaoService;
import com.moeasy.moeasy.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("account")
public class AccountController {

    private final KakaoService kakaoService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    @Hidden
    @GetMapping("/callback")
    public ResponseEntity<SuccessApiResponseDto<Map<String, Object>>> callback(HttpServletRequest request) throws Exception {
        KaKaoDto kakaoInfo = kakaoService.getKakaoInfo(request.getParameter("code"));

        Map<String, Object> data = getData(kakaoInfo);

        return ResponseEntity.ok()
                .body(
                        SuccessApiResponseDto.success(
                                200, "login success", data
                        )
                );
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> reissue(HttpServletRequest request, @RequestBody RefreshDto refreshTokenRequestDto) {
        // 1. 헤더에서 Access Token 추출
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(FailApiResponseDto.fail(HttpStatus.BAD_REQUEST.value(), "헤더에 유효한 Access Token이 없습니다."));
        }
        String accessToken = authorizationHeader.substring(7);

        // 2. Body에서 Refresh Token 추출
        String providedRefreshToken = refreshTokenRequestDto.getRefresh_token();
        if (providedRefreshToken == null || providedRefreshToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(FailApiResponseDto.fail(HttpStatus.BAD_REQUEST.value(), "Request Body에 Refresh Token이 없습니다."));
        }

        // 3. Access Token 유효성 검사
        String userEmail;
        boolean isAccessTokenExpired = false;
        try {
            userEmail = jwtUtil.extractEmail(accessToken);
            jwtUtil.validateToken(accessToken, userEmail);
        } catch (ExpiredJwtException e) {
            isAccessTokenExpired = true;
            userEmail = e.getClaims().getSubject();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(FailApiResponseDto.fail(HttpStatus.UNAUTHORIZED.value(), "Access Token이 유효하지 않습니다."));
        }

        if (!isAccessTokenExpired) {
            Map<String, String> tokenMap = new HashMap<>();
            tokenMap.put("access_token", accessToken);
            tokenMap.put("refresh_token", providedRefreshToken);
            return ResponseEntity.ok(SuccessApiResponseDto.success(200, "Access Token이 아직 유효합니다.", tokenMap));
        }

        // DB에서 Refresh Token 조회
        RefreshToken storedToken = refreshTokenRepository.findByUserEmail(userEmail)
                .orElse(null);

        // DB에 저장된 토큰이 없거나, 요청된 리프레시 토큰과 일치하지 않는 경우
        if (storedToken == null || !storedToken.getToken().equals(providedRefreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(FailApiResponseDto.fail(HttpStatus.UNAUTHORIZED.value(), "Refresh Token 정보가 일치하지 않습니다."));
        }

        // Refresh Token 만료 여부 검사
        try {
            jwtUtil.validateToken(providedRefreshToken, userEmail);
        } catch (Exception e) {
            refreshTokenRepository.delete(storedToken); // 만료되거나 유효하지 않은 토큰은 DB에서 삭제
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(FailApiResponseDto.fail(HttpStatus.UNAUTHORIZED.value(), "Refresh Token이 만료되었습니다. 다시 로그인해주세요."));
        }

        // Refresh Token이 유효하면 새 토큰 발급
        String newAccessToken = jwtUtil.generateAccessToken(userEmail);
        String newRefreshToken = jwtUtil.generateRefreshToken(userEmail);

        // DB의 Refresh Token을 새로 발급된 토큰으로 업데이트
        storedToken.updateToken(newRefreshToken);
        refreshTokenRepository.save(storedToken);

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("access_token", newAccessToken);
        tokenMap.put("refresh_token", newRefreshToken);

        return ResponseEntity.ok(SuccessApiResponseDto.success(200, "토큰이 성공적으로 갱신되었습니다.", tokenMap));
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String accessToken = request.getHeader("Authorization").substring(7);
        String userEmail = jwtUtil.extractEmail(accessToken);

        refreshTokenRepository.findByUserEmail(userEmail).ifPresent(refreshTokenRepository::delete);

        return ResponseEntity.ok(SuccessApiResponseDto.success(200, "logout success", null));
    }


    private List<String> getTokens(KaKaoDto kakaoInfo) {
        final String accessToken = jwtUtil.generateAccessToken(kakaoInfo.getEmail());
        final String refreshToken = jwtUtil.generateRefreshToken(kakaoInfo.getEmail());

        // Refresh Token DB에 저장 또는 업데이트
        refreshTokenRepository.findByUserEmail(kakaoInfo.getEmail())
                .ifPresentOrElse(
                        token -> token.updateToken(refreshToken),
                        () -> refreshTokenRepository.save(new RefreshToken(kakaoInfo.getEmail(), refreshToken))
                );

        return Arrays.asList(accessToken, refreshToken);
    }

    private Map<String, Object> getData(KaKaoDto kakaoInfo) {
        Map<String, Object> data = new HashMap<>();
        Map<String, String> token = new HashMap<>();

        List<String> tokens = getTokens(kakaoInfo);
        String access_token = tokens.get(0), refresh_token = tokens.get(1);

        token.put("access_token", access_token);
        token.put("refresh_token", refresh_token);

        data.put("name", kakaoInfo.getNickname());
        data.put("email", kakaoInfo.getEmail());
        data.put("token", token);
        return data;
    }
}
