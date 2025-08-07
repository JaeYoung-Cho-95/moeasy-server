package com.moeasy.moeasy.controller.question;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moeasy.moeasy.dto.llm.ChatRequestDto;
import com.moeasy.moeasy.response.SuccessApiResponseDto;
import com.moeasy.moeasy.service.account.CustomUserDetails;
import com.moeasy.moeasy.service.llm.OnBoardingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("questions")
public class OnBoardingController {

    private final OnBoardingService onBoardingService;
    private final ObjectMapper objectMapper;


    @PostMapping("/test")
    public ResponseEntity<SuccessApiResponseDto<Object>> testLLM(@AuthenticationPrincipal CustomUserDetails user, @RequestBody ChatRequestDto requestDto) throws JsonProcessingException {
        String llmResponse = onBoardingService.generate(requestDto);
        Object jsonResponse = objectMapper.readValue(llmResponse, Object.class);

        return ResponseEntity.ok()
                .body(SuccessApiResponseDto.success(200, "success", jsonResponse));
    }

}
