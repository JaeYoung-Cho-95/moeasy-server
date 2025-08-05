package com.moeasy.moeasy.dto.quesiton;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class VerifyQrCodeDto {
    private String questionId;
    private String expires;
    private String signature;
}
