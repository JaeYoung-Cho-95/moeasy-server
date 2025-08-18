package com.moeasy.moeasy.dto.account;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class AppleLoginDto {
    private String givenName;
    private String familyName;
    private String authCode;

    public String makeFullName() {
        return (givenName != null ? givenName : "") + (familyName != null ? familyName : "");
    }
}
