package com.moeasy.moeasy.dto.quesiton;

import com.moeasy.moeasy.dto.quesiton.enums.ProductType;
import lombok.Data;

@Data
public class OnboardingRequestDto {
    private ProductType productType;
    private String domain;
    private String purpose;
    private String description;
}
