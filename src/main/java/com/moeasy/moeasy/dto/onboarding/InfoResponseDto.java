package com.moeasy.moeasy.dto.onboarding;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InfoResponseDto {

  private final List<InfoItemDto> onBoardingItems;
}