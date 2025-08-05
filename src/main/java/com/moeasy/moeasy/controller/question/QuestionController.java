package com.moeasy.moeasy.controller.question;

import com.moeasy.moeasy.common.SuccessApiResponseDto;
import com.moeasy.moeasy.dto.quesiton.QuestionDto;
import com.moeasy.moeasy.dto.quesiton.MultipleChoiceQuestionDto;
import com.moeasy.moeasy.dto.quesiton.ShortAnswerQuestionDto;
import com.moeasy.moeasy.service.account.CustomUserDetails;
import com.moeasy.moeasy.service.question.MakeQuestionService;
import com.moeasy.moeasy.service.question.SaveQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("question")
public class QuestionController {

    private final MakeQuestionService makeQuestionService;
    private final SaveQuestionService saveQuestionService;

    @GetMapping("/make")
    public ResponseEntity<SuccessApiResponseDto> makeQuestions(@AuthenticationPrincipal User user) {

        List<MultipleChoiceQuestionDto> multipleChoiceQuestions = makeQuestionService.makeMultipleChoiceQuestions();
        List<ShortAnswerQuestionDto> shortAnswerQuestions = makeQuestionService.makeShortAnswerQuestions();

        QuestionDto QuestionDto = new QuestionDto(multipleChoiceQuestions, shortAnswerQuestions);


        return ResponseEntity.ok()
                .body(SuccessApiResponseDto.success(
                        200, "successfully generated the problems", QuestionDto
                ));
    }

    @PostMapping
    public ResponseEntity<SuccessApiResponseDto<Object>> saveQuestions(@AuthenticationPrincipal CustomUserDetails user, @RequestBody QuestionDto QuestionDto
    ) {
        Long id = user.getId();
        saveQuestionService.saveQuestionsJoinUser(id, QuestionDto);

        return ResponseEntity.ok()
                .body(SuccessApiResponseDto.success(201, "success", null));
    }
}
