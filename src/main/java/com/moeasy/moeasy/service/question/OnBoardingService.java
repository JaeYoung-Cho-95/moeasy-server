package com.moeasy.moeasy.service.question;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moeasy.moeasy.dto.llm.naver.NaverEmbeddingResponseDto;
import com.moeasy.moeasy.dto.llm.naver.PayloadDto;
import com.moeasy.moeasy.dto.llm.naver.RequestOnboardingDto;
import com.moeasy.moeasy.dto.llm.naver.RewriterRequestDto;
import com.moeasy.moeasy.dto.llm.naver.RewriterResponseDto;
import com.moeasy.moeasy.dto.llm.naver.searchFromVectorDBDto;
import com.moeasy.moeasy.dto.onboarding.OnboardingQuestionDto;
import com.moeasy.moeasy.dto.quesiton.OnboardingRequestDto;
import com.moeasy.moeasy.service.llm.NaverCloudStudioService;
import io.milvus.client.MilvusServiceClient;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.grpc.SearchResults;
import io.milvus.param.ConnectParam;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.dml.SearchParam;
import io.milvus.response.SearchResultsWrapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OnBoardingService extends NaverCloudStudioService {

  private final ObjectMapper objectMapper;

  private final MilvusServiceClient milvusServiceClient;

  public OnBoardingService(ResourceLoader resourceLoader,
      @Value("${naver.cloud.studio.host}") String host,
      @Value("${naver.cloud.studio.auth-token}") String authToken,
      @Value("${milvus.host}") String milvusHost,
      @Value("${milvus.port}") int milvusPort,
      @Value("${milvus.token}") String milvusToken,     // 예: "root:Milvus"
      @Value("${milvus.secure:false}") boolean secure,
      @Value("${milvus.connect-timeout-ms:10000}") long timeoutMs,
      ObjectMapper objectMapper
  ) {
    super(resourceLoader, host, authToken, objectMapper);
    this.objectMapper = objectMapper;
    this.milvusServiceClient = new MilvusServiceClient(
        ConnectParam.newBuilder()
            .withHost(milvusHost)
            .withPort(milvusPort)
            .withToken(milvusToken)
            .build()
    );
  }

  public List<OnboardingQuestionDto> makeOnBoardingQuestions(
      OnboardingRequestDto inputDto)
      throws JsonProcessingException {
    List<searchFromVectorDBDto> searchDto = searchTop3(inputDto, extractEmbedding(inputDto));

    RequestOnboardingDto requestDto = RequestOnboardingDto.from(inputDto, searchDto);

    return requestOnboardingQuestions(requestDto);
  }

  private List<Float> extractEmbedding(OnboardingRequestDto onboardingRequestDto) {
    String queryText = getQueryText(onboardingRequestDto);

    NaverEmbeddingResponseDto embedding = getEmbedding(queryText);
    return embedding.getResult().getEmbedding();
  }

  private String getQueryText(OnboardingRequestDto onboardingRequestDto) {
    String systemPrompt = getPromptWithFilePath("prompts/onboardingRewriter.txt");
    String queryText = String.valueOf(RewriterRequestDto.from(onboardingRequestDto));

    RewriterResponseDto rewriteResponse = chat(systemPrompt, queryText, new TypeReference<>() {
    });

    List<String> must =
        rewriteResponse.getMustTerms() != null ? rewriteResponse.getMustTerms() : List.of();
    List<String> should =
        rewriteResponse.getShouldTerms() != null ? rewriteResponse.getShouldTerms() : List.of();
    List<String> mustNot =
        rewriteResponse.getMustNotTerms() != null ? rewriteResponse.getMustNotTerms() : List.of();
    List<String> tagsAny =
        rewriteResponse.getTagsAny() != null ? rewriteResponse.getTagsAny() : List.of();

    // 2) must_not에 포함된 용어는 질의에서 제거
    if (!mustNot.isEmpty()) {
      var banned = mustNot.stream().map(String::trim).collect(Collectors.toSet());
      must = must.stream().filter(t -> !banned.contains(t)).toList();
      should = should.stream().filter(t -> !banned.contains(t)).toList();
      tagsAny = tagsAny.stream().filter(t -> !banned.contains(t)).toList();
    }

    // 3) 가중치 부여: must(×2) > tags_any(×2) > should(×1)
    String embedQuery =
        String.join(" ", must) + " " +
            String.join(" ", must) + " " +      // must 가중치
            String.join(" ", tagsAny) + " " +
            String.join(" ", tagsAny) + " " +   // tags_any 가중치
            String.join(" ", should);

    return embedQuery.trim();
  }


  private List<searchFromVectorDBDto> searchTop3(
      OnboardingRequestDto onboardingRequestDto
      , List<Float> embedding) {
    List<List<Float>> vectors = Collections.singletonList(embedding);
    String expr = String.format(
        "category == \"%s\" and domain == \"%s\"",
        onboardingRequestDto.getProductType(),
        onboardingRequestDto.getDomain()
    );

    SearchParam param = SearchParam.newBuilder()
        .withCollectionName("survey")
        .withConsistencyLevel(ConsistencyLevelEnum.BOUNDED)
        .withMetricType(MetricType.COSINE)
        .withTopK(5)
        .withVectors(vectors)
        .withVectorFieldName("vector")
        .withExpr(expr)
        .withOutFields(Arrays.asList("id", "category", "domain", "payload"))
        .withParams("{\"nprobe\":32}")
        .build();

    R<SearchResults> r = milvusServiceClient.search(param);
    if (r.getStatus() != R.Status.Success.getCode()) {
      throw new IllegalStateException("Milvus search failed: " + r.getMessage());
    }

    SearchResultsWrapper wrapper = new SearchResultsWrapper(r.getData().getResults());
    List<SearchResultsWrapper.IDScore> scores = wrapper.getIDScore(0);

    @SuppressWarnings("unchecked")
    List<String> payloadJsons = (List<String>) wrapper.getFieldData("payload", 0);

    List<searchFromVectorDBDto> top3 = new ArrayList<>();
    for (int i = 0; i < Math.min(3, scores.size()); i++) {
      try {
        // payload(JSON 문자열) -> PayloadDto
        PayloadDto payload = objectMapper.readValue(payloadJsons.get(i), PayloadDto.class);

        searchFromVectorDBDto dto = searchFromVectorDBDto.builder()
            .distance(scores.get(i).getScore())
            .payload(payload)
            .build();

        top3.add(dto);
      } catch (Exception e) {
        log.warn("payload 파싱 실패. index={}, reason={}", i, e.getMessage());
      }
    }
    return top3;
  }


  private List<OnboardingQuestionDto> requestOnboardingQuestions(
      RequestOnboardingDto requestOnboardingDto)
      throws JsonProcessingException {
    String systemPrompt = getPromptWithFilePath("prompts/onboardingMakeInfoQuestions.txt");
    String userPrompt = objectMapper.writeValueAsString(requestOnboardingDto);
    return chatHcx007(systemPrompt, userPrompt, new TypeReference<>() {
    });
  }
}