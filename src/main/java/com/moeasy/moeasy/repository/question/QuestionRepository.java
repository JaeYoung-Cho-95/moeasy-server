package com.moeasy.moeasy.repository.question;

import com.moeasy.moeasy.domain.question.Question;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

  Optional<Question> findById(Long questionId);

  Question save(Question question);

  List<Question> findAllByMember_IdOrderByCreatedTimeDesc(Long memberID);

  Question findBySurveyId(Long surveyId);
}
