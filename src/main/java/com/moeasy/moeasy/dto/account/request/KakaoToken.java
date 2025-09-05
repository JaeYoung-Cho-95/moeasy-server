package com.moeasy.moeasy.dto.account.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoToken {

  @JsonAlias("access_token")
  private String accessToken;
}
