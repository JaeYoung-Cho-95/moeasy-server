package com.moeasy.moeasy.service.question;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moeasy.moeasy.config.response.custom.CustomErrorException;
import com.moeasy.moeasy.domain.account.Member;
import com.moeasy.moeasy.domain.question.Question;
import com.moeasy.moeasy.domain.survey.Survey;
import com.moeasy.moeasy.dto.quesiton.MultipleChoiceIncludeIdQuestionDto;
import com.moeasy.moeasy.dto.quesiton.PatchQuestionTitleResponseDto;
import com.moeasy.moeasy.dto.quesiton.QuestionsDto;
import com.moeasy.moeasy.dto.quesiton.ShortAnswerIncludeIdQuestionDto;
import com.moeasy.moeasy.dto.quesiton.VerifyQrCodeDto;
import com.moeasy.moeasy.dto.quesiton.request.PatchQuestionTitleRequestDto;
import com.moeasy.moeasy.dto.quesiton.request.QuestionsRequestDto;
import com.moeasy.moeasy.dto.quesiton.response.QrCodeResponseDto;
import com.moeasy.moeasy.dto.quesiton.response.QuestionListResponseDto;
import com.moeasy.moeasy.dto.quesiton.response.QuestionsResponseDto;
import com.moeasy.moeasy.dto.survey.QuestionAnswerDto;
import com.moeasy.moeasy.dto.survey.SurveySaveDto;
import com.moeasy.moeasy.repository.account.MemberRepository;
import com.moeasy.moeasy.repository.question.QuestionRepository;
import com.moeasy.moeasy.repository.survey.SurveyRepository;
import com.moeasy.moeasy.service.account.CustomUserDetails;
import com.moeasy.moeasy.service.aws.AwsService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class QuestionService {

  private final QuestionRepository questionRepository;
  private final MemberRepository memberRepository;
  private final SurveyRepository surveyRepository;
  private final QrCodeService qrCodeService;
  private final AwsService awsService;
  private final ObjectMapper objectMapper;

  public QrCodeResponseDto saveQuestionsANdGetQrCodeS3Url(CustomUserDetails user,
      QuestionsRequestDto questionsRequestDto) {
    Long id = user.getId();
    Question question = saveQuestionsJoinUser(id, questionsRequestDto);
    return qrCodeService.makeQrCodeS3Url(question.getId());
  }

  public QuestionsResponseDto verifyQrCode(VerifyQrCodeDto dto) {
    Question question = qrCodeService.verifyQrCode(dto);
    QuestionsDto questionsDto = extractContent(question.getContent());

    return QuestionsResponseDto.from(
        question.getTitle(),
        questionsDto.getMultipleChoiceQuestions(),
        questionsDto.getShortAnswerQuestions()
    );
  }

  private QuestionsDto extractContent(String content) {
    try {
      return objectMapper.readValue(content, QuestionsDto.class);
    } catch (JsonProcessingException e) {
      throw CustomErrorException.from(HttpStatus.INTERNAL_SERVER_ERROR,
          "설문지 Json 으로 직렬화 중 error 발생");
    }
  }

  public Question saveQuestionsJoinUser(Long id, QuestionsRequestDto dto) {
    QuestionsDto questionsDto = QuestionsDto.from(dto);
    Survey survey = makeAndGetSurvey(questionsDto);
    Question question = createQuestion(dto, findMemberFyId(id), questionsDto);
    question.linkSurvey(survey);

    return questionRepository.save(question);
  }

  private Member findMemberFyId(Long id) {
    Optional<Member> findMember = memberRepository.findById(id);
    if (findMember.isEmpty()) {
      throw CustomErrorException.from(HttpStatus.NOT_FOUND, "user 정보를 조회할 수 없습니다.");
    }
    return findMember.get();
  }

  private Question createQuestion(
      QuestionsRequestDto questionsRequestDto,
      Member member,
      QuestionsDto questionsDto) {
    return Question.from(
        member,
        questionsRequestDto.getTitle(),
        makeQuestionsDataToJson(questionsDto)
    );
  }

  public PatchQuestionTitleResponseDto updateQuestionTitle(
      PatchQuestionTitleRequestDto dto) {
    Question question = updateQuestion(dto.getId(), dto.getTitle());
    return PatchQuestionTitleResponseDto.from(question);
  }

  public Question updateQuestion(Long id, String title) {
    Question question = questionRepository.findById(id)
        .orElseThrow(
            () -> CustomErrorException.from(HttpStatus.NOT_FOUND,
                "Id : " + id + " 에 해당하는 설문지를 찾을 수 없습니다."));

    question.updateTitle(title);
    return question;
  }

  private Survey makeAndGetSurvey(QuestionsDto questionsDto) {
    SurveySaveDto data = getSurveySaveDto(questionsDto);
    String surveyJson = makeSurveyDataToJson(data);
    Survey survey = Survey.from(surveyJson);
    surveyRepository.save(survey);
    return survey;
  }

  private String makeSurveyDataToJson(SurveySaveDto dto) {
    try {
      return objectMapper.writeValueAsString(dto);
    } catch (JsonProcessingException e) {
      throw CustomErrorException.from(HttpStatus.INTERNAL_SERVER_ERROR,
          "설문지에서 survey 형식 생성 중 json 변환 error 발생");
    }
  }

  private String makeQuestionsDataToJson(QuestionsDto dto) {
    try {
      return objectMapper.writeValueAsString(dto);
    } catch (JsonProcessingException e) {
      throw CustomErrorException.from(HttpStatus.INTERNAL_SERVER_ERROR,
          "설문지 json 으로 변환 중 error 발생");
    }
  }

  private SurveySaveDto getSurveySaveDto(
      QuestionsDto questionsDto) {

    List<MultipleChoiceIncludeIdQuestionDto> multipleChoiceQuestions = questionsDto.getMultipleChoiceQuestions();
    List<ShortAnswerIncludeIdQuestionDto> shortAnswerQuestions = questionsDto.getShortAnswerQuestions();
    List<Map<String, QuestionAnswerDto>> temp1 = new ArrayList<>();

    for (MultipleChoiceIncludeIdQuestionDto multipleChoiceQuestionDto : multipleChoiceQuestions) {
      String question = multipleChoiceQuestionDto.getQuestion();
      List<String> choices = multipleChoiceQuestionDto.getChoices();

      temp1.add(extractedSurveyForm(question, choices));
    }

    List<Map<String, QuestionAnswerDto>> temp2 = new ArrayList<>();
    for (ShortAnswerIncludeIdQuestionDto shortAnswerQuestionDto : shortAnswerQuestions) {
      String question = shortAnswerQuestionDto.getQuestion();
      temp2.add(extractedSurveyForm(question));
    }

    temp1.addAll(temp2);
    return SurveySaveDto.from(temp1);
  }

  private Map<String, QuestionAnswerDto> extractedSurveyForm(String question,
      List<String> choices) {
    Map<String, QuestionAnswerDto> temp = new LinkedHashMap<>();

    Map<String, Object> choiceCount = new HashMap<>();
    for (String choice : choices) {
      choiceCount.put(choice, 0);
    }
    choiceCount.put("others", new ArrayList<>());
    temp.put(question, QuestionAnswerDto.from(choiceCount));
    return temp;
  }

  private Map<String, QuestionAnswerDto> extractedSurveyForm(String question) {
    Map<String, QuestionAnswerDto> temp = new LinkedHashMap<>();
    Map<String, Object> choiceCount = new HashMap<>();
    choiceCount.put("others", new ArrayList<>());
    temp.put(question, QuestionAnswerDto.from(choiceCount));
    return temp;
  }

  @Transactional
  public List<Question> findAllByMemberAndRefresh(Long memberId) {
    List<Question> questions = questionRepository.findAllByMember_IdOrderByCreatedTimeDesc(
        memberId);
    LocalDateTime now = LocalDateTime.now();
    for (Question question : questions) {
      question.refreshExpired(now);
    }
    return questions;
  }

  public List<QuestionListResponseDto> findQuestionListByUser(CustomUserDetails user) {
    List<Question> questionList = findAllByMemberAndRefresh(user.getId());
    return questionList.stream()
        .map(q -> QuestionListResponseDto.from(
            q, awsService.generatePresignedUrl(q.getId() + "/qr_code.png", "qr_code"))
        ).toList();
  }
}
