package com.moeasy.moeasy.dto.account.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoUserDto {

  private long id;

  @JsonProperty("kakao_account")
  private KakaoAccount kakaoAccount;

  @Getter
  @NoArgsConstructor
  public static class KakaoAccount {

    private String email;
    private Profile profile;
  }

  @Getter
  @NoArgsConstructor
  public static class Profile {

    private String nickname;
  }
}
