package com.moeasy.moeasy.service.account;

import com.moeasy.moeasy.domain.account.Member;
import com.moeasy.moeasy.dto.account.KaKaoDto;
import com.moeasy.moeasy.service.jwt.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;


@Service
@RequiredArgsConstructor
public class KakaoService {

  private final MemberService memberService;
  private final JwtService jwtService;

  @Value("${kakao.client.id}")
  private String KAKAO_CLIENT_ID;

  @Value("${kakao.client.secret}")
  private String KAKAO_CLIENT_SECRET;

  @Value("${kakao.redirect.url}")
  private String KAKAO_REDIRECT_URL;

  private final static String KAKAO_AUTH_URI = "https://kauth.kakao.com";
  private final static String KAKAO_API_URI = "https://kapi.kakao.com";

  /**
   * kakao social 로그인 시 callback 할 url 을 정의 accessToken 과 refreshToken 을 반환
   */
  public String makeRedirectUrl(String code, HttpServletResponse response) throws Exception {
    // get 이메일 & 유저 닉네임
    KaKaoDto kakaoInfo = getKakaoInfo(code);

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
   * front 에서 요청한 uri 로 token 및 userinfo 담아 반환
   */
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

  private KaKaoDto getKakaoInfo(String code) throws Exception {
    if (code == null) {
      throw new Exception("Failed get authorization code");
    }

    String accessToken = "";

    try {
      HttpHeaders headers = new HttpHeaders();
      headers.add("Content-type", "application/x-www-form-urlencoded");

      MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
      params.add("grant_type", "authorization_code");
      params.add("client_id", KAKAO_CLIENT_ID);
      params.add("client_secret", KAKAO_CLIENT_SECRET);
      params.add("code", code);
      params.add("redirect_uri", KAKAO_REDIRECT_URL);

      RestTemplate restTemplate = new RestTemplate();
      HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers);

      ResponseEntity<String> response = restTemplate.exchange(
          KAKAO_AUTH_URI + "/oauth/token",
          HttpMethod.POST,
          httpEntity,
          String.class
      );

      JSONParser jsonParser = new JSONParser();
      JSONObject jsonObject = (JSONObject) jsonParser.parse(response.getBody());

      accessToken = (String) jsonObject.get("access_token");
    } catch (Exception e) {
      throw new Exception("API call failed");
    }
    return getUserInfoWithToken(accessToken);
  }

  public KaKaoDto getUserInfoWithToken(String accessToken) throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + accessToken);
    headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

    RestTemplate rt = new RestTemplate();
    HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(headers);
    ResponseEntity<String> response = rt.exchange(
        KAKAO_API_URI + "/v2/user/me",
        HttpMethod.POST,
        httpEntity,
        String.class
    );

    JSONParser jsonParser = new JSONParser();
    JSONObject jsonObject = (JSONObject) jsonParser.parse(response.getBody());
    JSONObject account = (JSONObject) jsonObject.get("kakao_account");
    JSONObject profile = (JSONObject) account.get("profile");

    long id = (long) jsonObject.get("id");
    String email = String.valueOf(account.get("email"));
    String nickname = String.valueOf(profile.get("nickname"));

    Member member = memberService.findOrCreateMember(email, nickname);

    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    HttpSession session = request.getSession();
    session.setAttribute("member", member);

    return KaKaoDto.builder()
        .id(id)
        .email(email)
        .nickname(nickname).build();
  }
}
