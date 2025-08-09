package com.moeasy.moeasy.service.survey;

import com.moeasy.moeasy.repository.survey.SurveyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SaveSurveyService {
    @Autowired private final SurveyRepository surveyRepository;


}
