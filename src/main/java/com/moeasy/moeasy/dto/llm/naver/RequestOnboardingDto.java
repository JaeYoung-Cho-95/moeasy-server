package com.moeasy.moeasy.dto.llm.naver;

import com.moeasy.moeasy.dto.quesiton.OnboardingRequestDto;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestOnboardingDto {

  private String purpose;
  private String description;
  private List<RetrievedItem> retrievedItems;

  public static RequestOnboardingDto from(OnboardingRequestDto onboardingRequestDto,
      List<searchFromVectorDBDto> selectedQuestionsInVectorDB) {

    List<RetrievedItem> items = selectedQuestionsInVectorDB == null
        ? List.of()
        : selectedQuestionsInVectorDB.stream()
            .filter(Objects::nonNull)
            .map(search -> RetrievedItem.from(search.getPayload()))
            .collect(Collectors.toList());

    return RequestOnboardingDto.builder()
        .purpose(onboardingRequestDto.getPurpose())
        .description(onboardingRequestDto.getDescription())
        .retrievedItems(items)
        .build();
  }
}
