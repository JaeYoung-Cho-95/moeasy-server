package com.moeasy.moeasy.dto.account.response;

import com.moeasy.moeasy.domain.account.Member;
import com.moeasy.moeasy.dto.account.request.KakaoUserDto;
import lombok.Builder;
import lombok.Data;

@Data
public class KakaoDto {

  private long id;
  private String email;
  private String nickname;

  @Builder
  public KakaoDto(long id, String email, String nickname) {
    this.id = id;
    this.email = email;
    this.nickname = nickname;
  }

  public static KakaoDto from(KakaoUserDto dto, Member member) {
    return KakaoDto.builder()
        .id(dto.getId())
        .email(member.getEmail())
        .nickname(member.getUsername())
        .build();
  }
}
