package com.moeasy.moeasy.controller.question;

import com.google.zxing.WriterException;
import com.moeasy.moeasy.common.ErrorApiResponseDto;
import com.moeasy.moeasy.common.FailApiResponseDto;
import com.moeasy.moeasy.common.SuccessApiResponseDto;
import com.moeasy.moeasy.domain.question.Question;
import com.moeasy.moeasy.dto.quesiton.QuestionDto;
import com.moeasy.moeasy.dto.quesiton.MultipleChoiceQuestionDto;
import com.moeasy.moeasy.dto.quesiton.ShortAnswerQuestionDto;
import com.moeasy.moeasy.dto.quesiton.VerifyQrCodeDto;
import com.moeasy.moeasy.service.account.CustomUserDetails;
import com.moeasy.moeasy.service.question.MakeQuestionService;
import com.moeasy.moeasy.service.question.QrCodeService;
import com.moeasy.moeasy.service.question.SaveQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("questions")
public class QuestionController {

    private final MakeQuestionService makeQuestionService;
    private final SaveQuestionService saveQuestionService;
    private final QrCodeService qrCodeService;

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
    public ResponseEntity<?> saveQuestions(@AuthenticationPrincipal CustomUserDetails user, @RequestBody QuestionDto QuestionDto
    ) {
        Long id = user.getId();
        Question question = saveQuestionService.saveQuestionsJoinUser(id, QuestionDto);
        Long questionId = question.getId();
        Map<String, String> data = new HashMap<>();

        try {
            data.put("qrCode", qrCodeService.getQrCodeS3Url(questionId));
        } catch (WriterException | IOException e) {
            ErrorApiResponseDto.ErrorResponse errorResponse = ErrorApiResponseDto.ErrorResponse.builder()
                    .type("WriterException")
                    .errorDetail(e.getMessage())
                    .build();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            ErrorApiResponseDto.error(500, errorResponse)
                    );
        }

        return ResponseEntity.ok()
                .body(SuccessApiResponseDto.success(201, "success", data));
    }

    @PostMapping("/verifyQrCode")
    public ResponseEntity<?> verifyExpireWithSignature(@RequestBody VerifyQrCodeDto verifyQrCodeDto) {
        Optional<Question> optionalQuestion = qrCodeService.verifyQrCode(verifyQrCodeDto);
        if (optionalQuestion.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            FailApiResponseDto.fail(
                                    410,
                                    "resource gone"
                            )
                    );
        }
        return ResponseEntity.ok()
                .body(
                        SuccessApiResponseDto.success(
                                200,
                                "success",
                                optionalQuestion.get().getContent()
                        )
                );
    }
}
