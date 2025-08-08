package com.moeasy.moeasy.dto.quesiton;

import lombok.Data;

import java.util.List;

@Data
public class ListMultipleChoicesQuestionDto {
    private List<MultipleChoiceQuestionDto> multipleChoiceQuestionDtoList;
}
