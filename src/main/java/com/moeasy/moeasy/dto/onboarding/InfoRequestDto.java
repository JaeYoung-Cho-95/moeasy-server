package com.moeasy.moeasy.dto.onboarding;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class InfoRequestDto {

  private List<String> information;
}
