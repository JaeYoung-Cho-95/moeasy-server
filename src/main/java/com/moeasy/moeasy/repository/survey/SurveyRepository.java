package com.moeasy.moeasy.repository.survey;

import com.moeasy.moeasy.domain.survey.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {
    Optional<Survey> findByQuestionId(Long questionId);

    List<Survey> findAllByLastUpdatedGreaterThanEqualAndLastUpdatedLessThan(
            LocalDateTime startInclusive, LocalDateTime endExclusive
    );
}
