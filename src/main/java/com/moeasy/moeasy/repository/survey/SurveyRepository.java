package com.moeasy.moeasy.repository.survey;

import com.moeasy.moeasy.domain.survey.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {
    Optional<Survey> findByQuestionId(Long questionId);
}
