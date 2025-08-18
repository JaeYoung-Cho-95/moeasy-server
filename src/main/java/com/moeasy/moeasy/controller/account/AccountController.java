package com.moeasy.moeasy.controller.account;

import com.moeasy.moeasy.domain.account.RefreshToken;
import com.moeasy.moeasy.dto.account.AppLoginTokenDto;
import com.moeasy.moeasy.dto.account.KaKaoDto;
import com.moeasy.moeasy.dto.account.MobileKakasSdkTokenDto;
import com.moeasy.moeasy.dto.account.ProfileDto;
import com.moeasy.moeasy.dto.account.RefreshDto;
import com.moeasy.moeasy.dto.account.TokenDto;
import com.moeasy.moeasy.jwt.JwtUtil;
import com.moeasy.moeasy.repository.account.RefreshTokenRepository;
import com.moeasy.moeasy.response.ErrorApiResponseDto;
<<<<<<< HEAD
import com.moeasy.moeasy.service.account.AppleService;
import com.moeasy.moeasy.service.account.CustomUserDetails;
import com.moeasy.moeasy.service.account.MemberService;
import com.moeasy.moeasy.service.aws.AwsService;
import io.swagger.v3.oas.annotations.media.ExampleObject;
=======
>>>>>>> 5950ba4 (debug. web 에서 refresh token 이슈. 원인 : web은 cookie 에 저장된 refresh token 접근 못하도록 막아놓고 내가 보냈었음)
import com.moeasy.moeasy.response.FailApiResponseDto;
import com.moeasy.moeasy.response.SuccessApiResponseDto;
import com.moeasy.moeasy.response.custom.CustomFailException;
import com.moeasy.moeasy.response.swagger.SwaggerExamples;
import com.moeasy.moeasy.service.account.CustomUserDetails;
import com.moeasy.moeasy.service.account.KakaoService;
import com.moeasy.moeasy.service.account.MemberService;
import com.moeasy.moeasy.service.aws.AwsService;
import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;


@Tag(name = "Account", description = "계정 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("account")
public class AccountController {

<<<<<<< HEAD
    private final KakaoService kakaoService;
    private final AppleService appleService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberService memberService;
    private final AwsService awsService;
=======
  private final KakaoService kakaoService;
  private final JwtUtil jwtUtil;
  private final RefreshTokenRepository refreshTokenRepository;
  private final MemberService memberService;
  private final AwsService awsService;
>>>>>>> 5950ba4 (debug. web 에서 refresh token 이슈. 원인 : web은 cookie 에 저장된 refresh token 접근 못하도록 막아놓고 내가 보냈었음)

  /**
   * 웹 소셜 로그인 callback (웹은 따로 로그인 없음)
   */
  @Hidden
  @GetMapping("/callback")
  public RedirectView callback(@RequestParam("code") String code, HttpServletResponse response)
      throws Exception {
    KaKaoDto kakaoInfo = kakaoService.getKakaoInfo(code);
    List<String> tokens = getTokens(kakaoInfo);
    String accessToken = tokens.get(0), refresh_token = tokens.get(1);

    ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refresh_token)
        .httpOnly(true)
        .secure(true) // HTTPS 환경에서만 동작
        .path("/")
        .maxAge(Duration.ofDays(7))
        .sameSite("Lax")
        .build();

    response.addHeader("Set-Cookie", refreshCookie.toString());

    String encodedNickname = URLEncoder.encode(kakaoInfo.getNickname(), StandardCharsets.UTF_8);
    String encodedEmail = URLEncoder.encode(kakaoInfo.getEmail(), StandardCharsets.UTF_8);

    // 프론트엔드로 access token 전달
    String redirectUrl = UriComponentsBuilder.fromUriString("https://mo-easy.com/auth/success")
        .queryParam("token", accessToken)
        .queryParam("email", encodedEmail)
        .queryParam("name", encodedNickname) // name에 한글이 있어도 자동으로 인코딩 처리됩니다.
        .build(false)
        .toUriString();

    return new RedirectView(redirectUrl);
  }

  /**
   * 모바일 앱 로그인 api
   */
  @Operation(
      summary = "모바일 앱 로그인",
      description = "카카오 SDK 액세스 토큰을 사용하여 로그인하고 JWT 토큰을 발급합니다."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "로그인 및 회원가입 성공(User 정보 없으면 회원가입 or 로그인)"),
      @ApiResponse(
          responseCode = "401",
          description = "유효하지 않은 카카오 토큰이거나 사용자 정보를 가져올 수 없는 경우",
          content = @Content(
              schema = @Schema(implementation = FailApiResponseDto.class),
              examples = @ExampleObject(
                  value = SwaggerExamples.INVALID_KAKAO_TOKEN_EXAMPLE // 상수 참조
              )
          )),
      @ApiResponse(
          responseCode = "500",
          description = "서버 에러 발생",
          content = @Content(
              schema = @Schema(implementation = ErrorApiResponseDto.class),
              examples = @ExampleObject(
                  value = SwaggerExamples.INTERNAL_SERVER_ERROR_EXAMPLE // 상수 참조
              )
          )),

  })
  @PostMapping("/login")
  public ResponseEntity<SuccessApiResponseDto<AppLoginTokenDto>> appLogin(
      @RequestBody MobileKakasSdkTokenDto mobileKakasSdkTokenDto) throws Exception {
    String kakaoAccessToken = mobileKakasSdkTokenDto.getAccessToken();

    KaKaoDto kakaoInfo = kakaoService.getUserInfoWithToken(kakaoAccessToken);

    if (kakaoInfo == null) {
      throw new CustomFailException(HttpStatus.UNAUTHORIZED,
          "유효하지 않은 카카오 토큰이거나 사용자 정보를 가져올 수 없습니다.");
    }

    List<String> tokens = getTokens(kakaoInfo);
    String accessToken = tokens.get(0), refreshToken = tokens.get(1);

    return ResponseEntity.ok()
        .body(
            SuccessApiResponseDto.success(200, "login success", AppLoginTokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(kakaoInfo.getEmail())
                .name(kakaoInfo.getNickname())
                .build()
            )
        );
  }

  @Operation(
      summary = "토큰 리프레쉬",
      description = "accessToken과 RefreshToken을 넘겨줬을 때 만료된 경우 새롭게 생성 후 전달하고, 만료되지 않은 경우 그대로 돌려줍니다.",
      security = @SecurityRequirement(name = "jwtAuth")
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "토큰 재발급 성공",
          content = @Content(
              schema = @Schema(implementation = SuccessApiResponseDto.class),
              examples = @ExampleObject(value = SwaggerExamples.REISSUE_SUCCESS_EXAMPLE))),
      @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰",
          content = @Content(
              schema = @Schema(implementation = FailApiResponseDto.class),
              examples = @ExampleObject(value = SwaggerExamples.INVALID_REFRESH_TOKEN_EXAMPLE))),
      @ApiResponse(responseCode = "500", description = "서버 에러 발생",
          content = @Content(
              schema = @Schema(implementation = ErrorApiResponseDto.class),
              examples = @ExampleObject(value = SwaggerExamples.INTERNAL_SERVER_ERROR_EXAMPLE)))
  })
  @PostMapping("/refresh")
  public ResponseEntity<?> reissue(HttpServletRequest request,
      @RequestBody(required = false) RefreshDto refreshTokenRequestDto) {

    // 1) 헤더에서 Access Token 추출
    String authorizationHeader = request.getHeader("Authorization");
    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(FailApiResponseDto.fail(HttpStatus.BAD_REQUEST.value(),
              "헤더에 유효한 Access Token이 없습니다."));
    }
    String accessToken = authorizationHeader.substring(7);

    // 2) Access Token 유효성 검사
    String userEmail;
    boolean isAccessTokenExpired = false;
    try {
      userEmail = jwtUtil.extractEmail(accessToken);
      jwtUtil.validateToken(accessToken, userEmail);
    } catch (ExpiredJwtException e) {
      isAccessTokenExpired = true;
      userEmail = e.getClaims().getSubject();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              FailApiResponseDto.fail(HttpStatus.UNAUTHORIZED.value(), "Access Token이 유효하지 않습니다."));
    }

<<<<<<< HEAD
    @PostMapping("/login/apple")
    public ResponseEntity<SuccessApiResponseDto<AppLoginTokenDto>> appleLogin(@RequestBody AppleLoginDto dto) throws Exception {
        // Apple 로그인은 사용자 정보를 처음으로 넘길때만 이름을 넘겨줌

        // Apple 서버에서 검증 이후 email 반환
        String email = appleService.verifyAuthorizationCode(dto);

        if (email == null) {
            throw new CustomFailException(HttpStatus.UNAUTHORIZED, "유효하지 않은 애플 토큰이거나 사용자 정보를 가져올 수 없습니다.");
        }

        List<String> tokens = getTokens(KaKaoDto.builder().email(email).build());
        String accessToken = tokens.get(0);
        String refreshToken = tokens.get(1);

        return ResponseEntity.ok()
                .body(
                        SuccessApiResponseDto.success(200, "login success", AppLoginTokenDto.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .email(email)
                                .name(dto.makeFullName())
                                .build()
                        )
                );
    }

    @Operation(
            summary = "토큰 리프레쉬",
            description = "accessToken과 RefreshToken을 넘겨줬을 때 만료된 경우 새롭게 생성 후 전달하고, 만료되지 않은 경우 그대로 돌려줍니다.",
            security = @SecurityRequirement(name = "jwtAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 재발급 성공",
                    content = @Content(
                            schema = @Schema(implementation = SuccessApiResponseDto.class),
                            examples = @ExampleObject(value = SwaggerExamples.REISSUE_SUCCESS_EXAMPLE))),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰",
                    content = @Content(
                            schema = @Schema(implementation = FailApiResponseDto.class),
                            examples = @ExampleObject(value = SwaggerExamples.INVALID_REFRESH_TOKEN_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = "서버 에러 발생",
                    content = @Content(
                            schema = @Schema(implementation = ErrorApiResponseDto.class),
                            examples = @ExampleObject(value = SwaggerExamples.INTERNAL_SERVER_ERROR_EXAMPLE)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> reissue(HttpServletRequest request, @RequestBody RefreshDto refreshTokenRequestDto) {
        // 1. 헤더에서 Access Token 추출
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(FailApiResponseDto.fail(HttpStatus.BAD_REQUEST.value(), "헤더에 유효한 Access Token이 없습니다."));
=======
    // 3) Refresh Token: 쿠키 우선 -> 바디 보조
    String providedRefreshToken = null;

    if (request.getCookies() != null) {
      for (jakarta.servlet.http.Cookie c : request.getCookies()) {
        if ("refresh_token".equals(c.getName())) {
          providedRefreshToken = c.getValue();
          break;
>>>>>>> 5950ba4 (debug. web 에서 refresh token 이슈. 원인 : web은 cookie 에 저장된 refresh token 접근 못하도록 막아놓고 내가 보냈었음)
        }
      }
    }

    if ((providedRefreshToken == null || providedRefreshToken.isEmpty())
        && refreshTokenRequestDto != null) {
      String bodyToken = refreshTokenRequestDto.getRefreshToken();
      if (bodyToken != null && !bodyToken.isEmpty()) {
        providedRefreshToken = bodyToken;
      }
    }

    if (providedRefreshToken == null || providedRefreshToken.isEmpty()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(FailApiResponseDto.fail(HttpStatus.BAD_REQUEST.value(),
              "Refresh Token이 제공되지 않았습니다."));
    }

    // 4) Access Token이 아직 유효하면 그대로 반환
    if (!isAccessTokenExpired) {
      Map<String, String> tokenMap = new HashMap<>();
      tokenMap.put("access_token", accessToken);
      tokenMap.put("refresh_token", providedRefreshToken);
      return ResponseEntity.ok(
          SuccessApiResponseDto.success(200, "Access Token이 아직 유효합니다.", tokenMap));
    }

<<<<<<< HEAD
    @Operation(
            summary = "유저 정보 조회",
            description = "accessToken을 Authorization 헤더에 담아 요청하면, name / email / profile url 을 반환합니다.",
            security = @SecurityRequirement(name = "jwtAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성송",
                    content = @Content(
                            schema = @Schema(implementation = SuccessApiResponseDto.class),
                            examples = @ExampleObject(value = SwaggerExamples.SUCCESS_LOGIN_EXAMPLE))),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰",
                    content = @Content(
                            schema = @Schema(implementation = FailApiResponseDto.class),
                            examples = @ExampleObject(value = SwaggerExamples.INVALID_ACCESS_TOKEN_EXAMPLE))),
    })
    @GetMapping
    public ResponseEntity<SuccessApiResponseDto<ProfileDto>> getProfileInfo(@AuthenticationPrincipal CustomUserDetails user) throws Exception {
        String presignedUrl = awsService.generatePresignedUrl(user.getProfileUrl(), "profile");
        return ResponseEntity.ok()
                .body(SuccessApiResponseDto.success(
                        200, "success", ProfileDto.builder()
                                .email(user.getEmail())
                                .name(user.getName())
                                .profileUrl(presignedUrl)
                                .build()
                ));
=======
    // 5) DB에서 Refresh Token 조회 및 일치/유효성 확인
    RefreshToken storedToken = refreshTokenRepository.findByUserEmail(userEmail).orElse(null);
    if (storedToken == null || !storedToken.getToken().equals(providedRefreshToken)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(FailApiResponseDto.fail(HttpStatus.UNAUTHORIZED.value(),
              "Refresh Token 정보가 일치하지 않습니다."));
>>>>>>> 5950ba4 (debug. web 에서 refresh token 이슈. 원인 : web은 cookie 에 저장된 refresh token 접근 못하도록 막아놓고 내가 보냈었음)
    }

    try {
      jwtUtil.validateToken(providedRefreshToken, userEmail);
    } catch (Exception e) {
      refreshTokenRepository.delete(storedToken); // 만료/유효하지 않은 토큰 정리
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(FailApiResponseDto.fail(HttpStatus.UNAUTHORIZED.value(),
              "Refresh Token이 만료되었습니다. 다시 로그인해주세요."));
    }

    // 6) 새 토큰 발급 + DB 업데이트
    String newAccessToken = jwtUtil.generateAccessToken(userEmail);
    String newRefreshToken = jwtUtil.generateRefreshToken(userEmail);

    storedToken.updateToken(newRefreshToken);
    refreshTokenRepository.save(storedToken);

    // 7) 웹용: HttpOnly 쿠키로 재설정, 앱용: Body에도 포함
    ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", newRefreshToken)
        .httpOnly(true)
        .secure(true)
        .path("/")
        .maxAge(java.time.Duration.ofDays(7))
        .sameSite("Lax") // 크로스 사이트 호출이면 "None"으로 조정 필요
        .build();

    TokenDto tokenDto = TokenDto.builder()
        .accessToken(newAccessToken)
        .refreshToken(newRefreshToken) // 앱은 이 값으로 보안 저장소 갱신
        .build();

    return ResponseEntity.ok()
        .header("Set-Cookie", refreshCookie.toString())
        .body(SuccessApiResponseDto.success(200, "토큰이 성공적으로 갱신되었습니다.", tokenDto));
  }

  @Operation(
      summary = "로그아웃",
      description = "accessToken을 Authorization 헤더에 담아 요청하면, 서버에 저장된 refresh token을 삭제하여 로그아웃 처리합니다.",
      security = @SecurityRequirement(name = "jwtAuth")
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "로그아웃 성공",
          content = @Content(
              schema = @Schema(implementation = SuccessApiResponseDto.class),
              examples = @ExampleObject(value = SwaggerExamples.LOGOUT_SUCCESS_EXAMPLE))),
      @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰",
          content = @Content(
              schema = @Schema(implementation = FailApiResponseDto.class),
              examples = @ExampleObject(value = SwaggerExamples.INVALID_ACCESS_TOKEN_EXAMPLE))),
      @ApiResponse(responseCode = "500", description = "서버 에러 발생",
          content = @Content(
              schema = @Schema(implementation = ErrorApiResponseDto.class),
              examples = @ExampleObject(value = SwaggerExamples.INTERNAL_SERVER_ERROR_EXAMPLE)))
  })
  @PostMapping("/logout")
  public ResponseEntity<?> logout(HttpServletRequest request) {
    String accessToken = request.getHeader("Authorization").substring(7);
    String userEmail = jwtUtil.extractEmail(accessToken);

    refreshTokenRepository.findByUserEmail(userEmail).ifPresent(refreshTokenRepository::delete);

    // 웹 쿠키 제거
    ResponseCookie deleteRefreshCookie = ResponseCookie.from("refresh_token", "")
        .httpOnly(true)
        .secure(true)
        .path("/")
        .maxAge(0)
        .sameSite("Lax")
        .build();

    return ResponseEntity.ok()
        .header("Set-Cookie", deleteRefreshCookie.toString())
        .body(SuccessApiResponseDto.success(200, "logout success", null));
  }

  @Operation(summary = "회원 탈퇴", description = "인증된 사용자의 계정을 삭제합니다. (설문지, refresh token 모두 cascade 로 삭제됩니다)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "회원 탈퇴 성공 (내용 없음)"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
  })
  @DeleteMapping
  public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal CustomUserDetails user)
      throws Exception {
    memberService.deleteMember(user.getId());
    return ResponseEntity.noContent().build();
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


  @Operation(
      summary = "유저 정보 조회",
      description = "accessToken을 Authorization 헤더에 담아 요청하면, name / email / profile url 을 반환합니다.",
      security = @SecurityRequirement(name = "jwtAuth")
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "조회 성송",
          content = @Content(
              schema = @Schema(implementation = SuccessApiResponseDto.class),
              examples = @ExampleObject(value = SwaggerExamples.SUCCESS_LOGIN_EXAMPLE))),
      @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰",
          content = @Content(
              schema = @Schema(implementation = FailApiResponseDto.class),
              examples = @ExampleObject(value = SwaggerExamples.INVALID_ACCESS_TOKEN_EXAMPLE))),
  })
  @GetMapping
  public ResponseEntity<SuccessApiResponseDto<ProfileDto>> getProfileInfo(
      @AuthenticationPrincipal CustomUserDetails user) throws Exception {
    String presignedUrl = awsService.generatePresignedUrl(user.getProfileUrl(), "profile");
    return ResponseEntity.ok()
        .body(SuccessApiResponseDto.success(
            200, "success", ProfileDto.builder()
                .email(user.getEmail())
                .name(user.getName())
                .profileUrl(presignedUrl)
                .build()
        ));
  }
}