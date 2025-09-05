package com.moeasy.moeasy.service.account;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moeasy.moeasy.config.response.custom.CustomErrorException;
import com.moeasy.moeasy.domain.account.Member;
import com.moeasy.moeasy.dto.account.MobileKakasSdkTokenDto;
import com.moeasy.moeasy.dto.account.request.KakaoToken;
import com.moeasy.moeasy.dto.account.request.KakaoUserDto;
import com.moeasy.moeasy.dto.account.response.AppLoginDataDto;
import com.moeasy.moeasy.dto.account.response.KakaoDto;
import com.moeasy.moeasy.service.jwt.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class KakaoService {

  private final MemberService memberService;
  private final JwtService jwtService;
  private final ObjectMapper objectMapper;

  @Value("${kakao.client.id}")
  private String KAKAO_CLIENT_ID;

  @Value("${kakao.client.secret}")
  private String KAKAO_CLIENT_SECRET;

  @Value("${kakao.redirect.url}")
  private String KAKAO_REDIRECT_URL;

  @Value("${kakao.etc.auth_uri}")
  private String KAKAO_AUTH_URI;

  @Value("${kakao.etc.api_uri}")
  private String KAKAO_API_URI;

  /**
   * kakao social 로그인 시 callback 할 url 을 정의 accessToken 과 refreshToken 을 반환
   */
  public String makeRedirectUrl(String code, HttpServletResponse response) {
    KakaoDto kakaoInfo = getKakaoInfo(code);

    // 서버에서 accessToken, refreshToken 발급 > refreshToken 는 header 에 담기
    List<String> tokens = jwtService.getTokens(kakaoInfo);
    String accessToken = tokens.get(0), refreshToken = tokens.get(1);
    response.addHeader("Set-Cookie", jwtService.makeResponseCookie(refreshToken).toString());

    // 한글 깨지지 않게 인코딩
    String encodedNickname = URLEncoder.encode(kakaoInfo.getNickname(), StandardCharsets.UTF_8);
    String encodedEmail = URLEncoder.encode(kakaoInfo.getEmail(), StandardCharsets.UTF_8);

    return getUriString(accessToken, encodedEmail, encodedNickname);
  }

  /**
   * 모바일 social 로그인 (카카오) 의 경우 카카오 oauth 에서 발급받은 access token, refresh token 을 이용해 userInfo 요청 이후 해당
   * 고객의 정보로 서버에서 발급한 JWT 생성 후 반환
   */
  public AppLoginDataDto getAppLoginDataDto(MobileKakasSdkTokenDto dto) {
    // get accessToken from a client
    String accessToken = dto.getAccessToken();

    // get userInfo from a Kakao oauth
    KakaoDto userInfo = getUserInfoWithToken(accessToken);

    // make accessToken and refreshToken using userInfo
    List<String> tokens = jwtService.getTokens(userInfo);

    return AppLoginDataDto.from(userInfo, tokens);
  }


  @NotNull
  private static String getUriString(String accessToken, String encodedEmail,
      String encodedNickname) {
    return UriComponentsBuilder.fromUriString("https://mo-easy.com/auth/success")
        .queryParam("token", accessToken)
        .queryParam("email", encodedEmail)
        .queryParam("name", encodedNickname)
        .build(false)
        .toUriString();
  }


  private KakaoDto getKakaoInfo(String code) {
    validateCode(code);

    KakaoToken kakaoToken = readKakaoToken(getUserAccessTokenFromKakao(code));
    return getUserInfoWithToken(kakaoToken.getAccessToken());
  }

  private KakaoToken readKakaoToken(String tokenFromKakao) {
    try {
      return objectMapper.readValue(tokenFromKakao, KakaoToken.class);
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      throw CustomErrorException.from(HttpStatus.INTERNAL_SERVER_ERROR,
          "failed to parse kakao token response");
    }
  }

  private static void validateCode(String code) {
    if (code == null || code.isBlank()) {
      throw CustomErrorException.from(HttpStatus.BAD_REQUEST,
          "authorization code is missing");
    }
  }


  private String getUserAccessTokenFromKakao(String code) {
    RestTemplate restTemplate = new RestTemplate();
    return restTemplate.exchange(
        KAKAO_AUTH_URI + "/oauth/token",
        HttpMethod.POST,
        getHttpEntity(code),
        String.class).getBody();
  }


  private HttpEntity<MultiValueMap<String, String>> getHttpEntity(String code) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-type", "application/x-www-form-urlencoded");
    MultiValueMap<String, String> params = getStringStringMultiValueMap(code);
    return new HttpEntity<>(params, headers);
  }


  private MultiValueMap<String, String> getStringStringMultiValueMap(String code) {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("grant_type", "authorization_code");
    params.add("client_id", KAKAO_CLIENT_ID);
    params.add("client_secret", KAKAO_CLIENT_SECRET);
    params.add("code", code);
    params.add("redirect_uri", KAKAO_REDIRECT_URL);
    return params;
  }


  public KakaoDto getUserInfoWithToken(String accessToken) {
    KakaoUserDto dto = getKakaoUserDto(accessToken);
    Member member = getMember(dto);
    return KakaoDto.from(dto, member);
  }

  private Member getMember(KakaoUserDto dto) {
    return memberService.findOrCreateMember(
        dto.getKakaoAccount().getEmail(),
        dto.getKakaoAccount().getProfile().getNickname()
    );
  }

  private KakaoUserDto getKakaoUserDto(String accessToken) {
    try {
      return objectMapper.readValue(
          getUserInfoFromKakao(accessToken),
          KakaoUserDto.class);
    } catch (JsonProcessingException e) {
      throw CustomErrorException.from(HttpStatus.INTERNAL_SERVER_ERROR,
          "failed to parse userinfo in response");
    }
  }

  private String getUserInfoFromKakao(String accessToken) {
    HttpHeaders headers = getHttpHeaders(accessToken);
    RestTemplate restTemplate = new RestTemplate();
    return requestUserInfoToKakao(restTemplate, headers).getBody();
  }

  private ResponseEntity<String> requestUserInfoToKakao(RestTemplate restTemplate,
      HttpHeaders headers) {
    return restTemplate.exchange(
        KAKAO_API_URI + "/v2/user/me",
        HttpMethod.POST,
        new HttpEntity<>(headers),
        String.class
    );
  }

  private HttpHeaders getHttpHeaders(String accessToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + accessToken);
    headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
    return headers;
  }
}
