package com.moeasy.moeasy.dto.quesiton;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
public class ListShortAnswerQuestionDto {
    private List<ShortAnswerQuestionDto> multipleChoiceQuestionDtoList;
}
