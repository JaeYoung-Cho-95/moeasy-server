package com.moeasy.moeasy.dto.account;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class MobileKakasSdkTokenDto {
    private String accessToken;
    private String refreshToken;
}
