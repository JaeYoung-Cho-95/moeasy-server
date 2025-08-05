package com.moeasy.moeasy.service.question;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moeasy.moeasy.domain.account.Member;
import com.moeasy.moeasy.domain.question.Question;
import com.moeasy.moeasy.dto.quesiton.QuestionDto;
import com.moeasy.moeasy.repository.account.MemberRepository;
import com.moeasy.moeasy.repository.question.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class SaveQuestionService {

    private final QuestionRepository questionRepository;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;

    public Question saveQuestionsJoinUser(Long id, QuestionDto questionDto) {
        Optional<Member> findMember = memberRepository.findById(id);
        if (findMember.isEmpty()) throw new IllegalArgumentException("해당 ID의 회원을 찾을 수 없습니다: " + id);

        try {
            String questionJson = objectMapper.writeValueAsString(questionDto);
            Member member = findMember.get();
            Question question = Question.builder()
                    .member(member)
                    .content(questionJson)
                    .build();
            return questionRepository.save(question);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("\"QuestionDto를 JSON으로 변환하는 중 오류가 발생했습니다. : " + e);
        }
    }
}
