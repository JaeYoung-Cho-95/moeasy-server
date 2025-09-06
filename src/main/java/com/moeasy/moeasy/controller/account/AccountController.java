package com.moeasy.moeasy.controller.account;

import com.moeasy.moeasy.config.jwt.JwtUtil;
import com.moeasy.moeasy.config.response.responseDto.ErrorResponseDto;
import com.moeasy.moeasy.config.response.responseDto.SuccessResponseDto;
import com.moeasy.moeasy.config.swagger.SwaggerExamples;
import com.moeasy.moeasy.dto.account.MobileKakasSdkTokenDto;
import com.moeasy.moeasy.dto.account.ProfileDto;
import com.moeasy.moeasy.dto.account.request.RefreshTokenForAppDto;
import com.moeasy.moeasy.dto.account.response.AppLoginDataDto;
import com.moeasy.moeasy.dto.account.response.RefreshTokensDto;
import com.moeasy.moeasy.repository.account.RefreshTokenRepository;
import com.moeasy.moeasy.service.account.CustomUserDetails;
import com.moeasy.moeasy.service.account.KakaoService;
import com.moeasy.moeasy.service.account.MemberService;
import com.moeasy.moeasy.service.aws.AwsService;
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
import lombok.RequiredArgsConstructor;
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

@Tag(name = "Account", description = "계정 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("account")
@ApiResponses(value = {
    @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰",
        content = @Content(
            schema = @Schema(implementation = ErrorResponseDto.class),
            examples = @ExampleObject(value = SwaggerExamples.INVALID_REFRESH_TOKEN_EXAMPLE))),
    @ApiResponse(responseCode = "500", description = "서버 에러 발생",
        content = @Content(
            schema = @Schema(implementation = ErrorResponseDto.class),
            examples = @ExampleObject(value = SwaggerExamples.INTERNAL_SERVER_ERROR_EXAMPLE)))
})
public class AccountController {

  private final KakaoService kakaoService;
  private final JwtUtil jwtUtil;
  private final RefreshTokenRepository refreshTokenRepository;
  private final MemberService memberService;
  private final AwsService awsService;

  /**
   * 웹 - 카카오 로그인 callback (웹은 따로 로그인 없음)
   */
  @Hidden
  @GetMapping("/callback")
  public RedirectView callback(@RequestParam("code") String code, HttpServletResponse response)
      throws Exception {
    return new RedirectView(kakaoService.makeRedirectUrl(code, response));
  }

  /**
   * 모바일 앱 로그인 api
   */
  @Operation(
      summary = "모바일 앱 로그인",
      description = "카카오 SDK 액세스 토큰을 사용하여 로그인하고 JWT 토큰을 발급합니다.")
  @PostMapping("/login")
  public AppLoginDataDto appLogin(
      @RequestBody MobileKakasSdkTokenDto dto) {
    return kakaoService.getAppLoginDataDto(dto);
  }


  /**
   * refreshToken 이용한 token refreh api
   */
  @Operation(
      summary = "토큰 리프레쉬",
      description = "accessToken과 RefreshToken을 넘겨줬을 때 만료된 경우 새롭게 생성 후 전달하고, 만료되지 않은 경우 그대로 돌려줍니다.",
      security = @SecurityRequirement(name = "jwtAuth")
  )
  @PostMapping("/refresh")
  public RefreshTokensDto refresh(
      HttpServletRequest request,
      HttpServletResponse response,
      @RequestBody(required = false) RefreshTokenForAppDto refreshTokenRequestDto) {
    RefreshTokensDto refreshToken = jwtUtil.getRefreshToken(
        request,
        refreshTokenRequestDto
    );
    setRefreshTokenInCookie(response, refreshToken.getRefreshToken());
    return refreshToken;
  }


  /**
   * 로그아웃 api. refreshToken 삭제 및 cookie 에서 refreshToken 삭제
   */
  @Operation(
      summary = "로그아웃",
      description = "accessToken을 Authorization 헤더에 담아 요청하면, 서버에 저장된 refresh token을 삭제하여 로그아웃 처리합니다.",
      security = @SecurityRequirement(name = "jwtAuth")
  )
  @PostMapping("/logout")
  public ResponseEntity<Void> logout(
      HttpServletRequest request,
      HttpServletResponse response) {
    jwtUtil.deleteRefreshToken(request);
    setRefreshTokenInCookie(response, "");

    return ResponseEntity.noContent().build();
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

  @Operation(
      summary = "유저 정보 조회",
      description = "accessToken을 Authorization 헤더에 담아 요청하면, name / email / profile url 을 반환합니다.",
      security = @SecurityRequirement(name = "jwtAuth")
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "조회 성공",
          content = @Content(
              schema = @Schema(implementation = SuccessResponseDto.class),
              examples = @ExampleObject(value = SwaggerExamples.SUCCESS_LOGIN_EXAMPLE))),
      @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰",
          content = @Content(
              schema = @Schema(implementation = ErrorResponseDto.class),
              examples = @ExampleObject(value = SwaggerExamples.INVALID_ACCESS_TOKEN_EXAMPLE))),
  })
  @GetMapping
  public ResponseEntity<SuccessResponseDto<ProfileDto>> getProfileInfo(
      @AuthenticationPrincipal CustomUserDetails user) throws Exception {
    String presignedUrl = awsService.generatePresignedUrl(user.getProfileUrl(), "profile");
    return ResponseEntity.ok()
        .body(SuccessResponseDto.success(
            200, "success", ProfileDto.builder()
                .email(user.getEmail())
                .name(user.getName())
                .profileUrl(presignedUrl)
                .build()
        ));
  }


  private static void setRefreshTokenInCookie(HttpServletResponse response, String Content) {
    ResponseCookie refreshCookie = ResponseCookie.from(
            "refresh_token",
            Content)
        .httpOnly(true)
        .secure(true)
        .path("/")
        .maxAge(java.time.Duration.ofDays(7))
        .sameSite("Lax")
        .build();

    response.addHeader("Set-Cookie", refreshCookie.toString());
  }
}