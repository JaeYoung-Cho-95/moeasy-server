//package com.moeasy.moeasy.controller.question;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.moeasy.moeasy.dto.llm.ChatRequestDto;
//import com.moeasy.moeasy.response.SuccessApiResponseDto;
//import com.moeasy.moeasy.service.account.CustomUserDetails;
//import com.moeasy.moeasy.service.llm.OnBoardingService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("questions")
//public class OnBoardingController {
//
//    private final OnBoardingService onBoardingService;
//
//
//    @PostMapping("/test")
//    public ResponseEntity<SuccessApiResponseDto<Object>> testLLM(@AuthenticationPrincipal CustomUserDetails user, @RequestBody ChatRequestDto requestDto) throws JsonProcessingException {
//        List<String> llmResponse = onBoardingService.generate(requestDto);
//
//        return ResponseEntity.ok()
//                .body(SuccessApiResponseDto.success(200, "success", llmResponse));
//    }
//
//}
