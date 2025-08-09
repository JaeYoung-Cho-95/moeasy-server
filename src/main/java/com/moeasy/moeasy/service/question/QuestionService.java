package com.moeasy.moeasy.service.question;

import com.moeasy.moeasy.domain.question.Question;
import com.moeasy.moeasy.repository.question.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionService {

    private final QuestionRepository questionRepository;

    @Transactional
    public List<Question> findAllByMemberAndRefresh(Long memberId) {
        List<Question> questions = questionRepository.findAllByMember_IdOrderByCreatedTimeDesc(memberId);
        LocalDateTime now = LocalDateTime.now();
        for (Question q : questions) {
            q.refreshExpired(now);
        }
        return questions;
    }
}
