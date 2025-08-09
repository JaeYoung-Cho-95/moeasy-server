package com.moeasy.moeasy.controller.question;

import com.moeasy.moeasy.dto.survey.SurveySaveRequestDto;
import com.moeasy.moeasy.response.SuccessApiResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("suvery")
public class SurveyController {

    @PostMapping
    public SuccessApiResponseDto saveSurvey(@RequestBody SurveySaveRequestDto surveySaveRequestDto) {
        return SuccessApiResponseDto.success(200, "success", null);
    }
}
