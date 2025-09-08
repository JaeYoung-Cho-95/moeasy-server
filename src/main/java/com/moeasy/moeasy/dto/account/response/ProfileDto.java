package com.moeasy.moeasy.dto.account.response;

import com.moeasy.moeasy.service.account.CustomUserDetails;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "user 정보", description = "유저 프로필 정보 조회")
public class ProfileDto {

  @Schema(description = "유저 email", example = "test12@test.com")
  private final String email;

  @Schema(description = "유저 name", example = "김옥순")
  private final String name;

  @Schema(description = "프로필 이미지 다운이 가능한 aws s3 url", example = "https://test-bucket.s3.ap-northeast-2.amazonm.....312daslk33")
  private final String profileUrl;

  public static ProfileDto from(CustomUserDetails user, String presigendUrl) {
    return ProfileDto.builder()
        .email(user.getEmail())
        .name(user.getName())
        .profileUrl(presigendUrl)
        .build();
  }
}
