package com.moeasy.moeasy.controller.question;

import com.moeasy.moeasy.common.SuccessApiResponseDto;
import com.moeasy.moeasy.dto.quesiton.MakeQuestionDto;
import com.moeasy.moeasy.service.question.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("question")
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping("/make")
    public ResponseEntity<SuccessApiResponseDto> makeQuestions(@AuthenticationPrincipal User user) {

        List<Map<String, List<String>>> multipleChoiceQuestions = questionService.makeMultipleChoiceQuestions();
        List<Map<String, List<String>>> shortAnswerQuestions = questionService.makeShortAnswerQuestions();

        MakeQuestionDto makeQuestionDto = new MakeQuestionDto(multipleChoiceQuestions, shortAnswerQuestions);

        return ResponseEntity.ok()
                .body(SuccessApiResponseDto.success(
                        200, "successfully generated the problems", makeQuestionDto
                ));
    }
}
