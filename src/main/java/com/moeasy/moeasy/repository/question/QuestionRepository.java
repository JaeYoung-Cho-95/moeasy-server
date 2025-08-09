package com.moeasy.moeasy.repository.question;

import com.moeasy.moeasy.domain.question.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    Question save(Question question);

    List<Question> findAllByMember_IdOrderByCreatedTimeDesc(Long memberID);
}
