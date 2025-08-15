package com.moeasy.moeasy.service.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moeasy.moeasy.dto.llm.naver.ContentPart;
import com.moeasy.moeasy.dto.llm.naver.MessageDto;
import com.moeasy.moeasy.dto.llm.naver.NaverChatHcx007RequestDto;
import com.moeasy.moeasy.dto.llm.naver.NaverChatRequestDto;
import com.moeasy.moeasy.dto.llm.naver.NaverChatResponseDto;
import com.moeasy.moeasy.dto.llm.naver.NaverEmbeddingResponseDto;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class NaverCloudStudioService implements NaverCloudStudio {

  private final ResourceLoader resourceLoader;
  private final WebClient webClient;
  private final ObjectMapper objectMapper;

  public NaverCloudStudioService(ResourceLoader resourceLoader,
      @Value("${naver.cloud.studio.host}") String host,
      @Value("${naver.cloud.studio.auth-token}") String authToken, ObjectMapper objectMapper) {
    this.resourceLoader = resourceLoader;
    this.objectMapper = objectMapper;
    this.webClient = WebClient.builder()
        .baseUrl(host)
        .defaultHeader("Authorization", "Bearer " + authToken)
        .defaultHeader("Content-Type", "application/json")
        .build();
  }

  @Override
  public <T> T chat(String systemPrompt, String userPrompt, Class<T> responseType) {
    NaverChatResponseDto naverChatResponseDto = getNaverChatResponse(systemPrompt, userPrompt);

    if (naverChatResponseDto != null && "20000".equals(
        naverChatResponseDto.getStatus().getCode())) {
      String response = cleanResponse(naverChatResponseDto.getFirstTextMessage());
      log.info(response);
      try {
        return objectMapper.readValue(response, responseType);
      } catch (JsonProcessingException e) {
        // Fallback: 혼합 응답에서 JSON만 추출하여 재시도
        String candidate = extractFirstJson(response);
        log.info("Fallback JSON candidate: {}", candidate);
        try {
          return objectMapper.readValue(candidate, responseType);
        } catch (JsonProcessingException ex) {
          throw new RuntimeException("JSON 파싱에 실패했습니다. " + ex.getMessage(), ex);
        }
      }
    } else {
      String errorMessage =
          naverChatResponseDto != null ? naverChatResponseDto.getStatus().getMessage()
              : "Unknown error";
      throw new RuntimeException("네이버 클라우드 API 호출에 실패했습니다: " + errorMessage);
    }
  }

  @Override
  public <T> T chat(String systemPrompt, String userPrompt, TypeReference<T> typeReference) {
    NaverChatResponseDto naverChatResponseDto = getNaverChatResponse(systemPrompt, userPrompt);

    if (naverChatResponseDto != null && "20000".equals(
        naverChatResponseDto.getStatus().getCode())) {
      String response = cleanResponse(naverChatResponseDto.getFirstTextMessage());
      log.info(response);
      try {
        return objectMapper.readValue(response, typeReference);
      } catch (JsonProcessingException e) {
        String candidate = extractFirstJson(response);
        log.info("Fallback JSON candidate: {}", candidate);
        try {
          return objectMapper.readValue(candidate, typeReference);
        } catch (JsonProcessingException ex) {
          throw new RuntimeException("JSON 파싱에 실패했습니다. " + ex.getMessage(), ex);
        }
      }
    } else {
      String errorMessage =
          naverChatResponseDto != null ? naverChatResponseDto.getStatus().getMessage()
              : "Unknown error";
      throw new RuntimeException("네이버 클라우드 API 호출에 실패했습니다: " + errorMessage);
    }
  }

  @Override
  public <T> T chatHcx007(String systemPrompt, String userPrompt, TypeReference<T> typeReference) {
    NaverChatResponseDto naverChatResponseDto = getNaverChatHcx007Response(systemPrompt,
        userPrompt);

    if (naverChatResponseDto != null && "20000".equals(
        naverChatResponseDto.getStatus().getCode())) {
      String response = cleanResponse(naverChatResponseDto.getFirstTextMessage());
      log.info("response : " + response);
      try {
        return objectMapper.readValue(response, typeReference);
      } catch (JsonProcessingException e) {
        // HCX-007 혼합 응답(JSON + 검토 사항 등) 대비
        String candidate = extractFirstJson(response);
        log.info("Fallback JSON candidate: {}", candidate);
        try {
          return objectMapper.readValue(candidate, typeReference);
        } catch (JsonProcessingException ex) {
          log.error(ex.getOriginalMessage());
          log.error(ex.getMessage());
          throw new RuntimeException("JSON 파싱에 실패했습니다. " + ex.getMessage(), ex);
        }
      }
    } else {
      String errorMessage =
          naverChatResponseDto != null ? naverChatResponseDto.getStatus().getMessage()
              : "Unknown error";
      throw new RuntimeException("네이버 클라우드 API 호출에 실패했습니다: " + errorMessage);
    }
  }

  @Override
  public String getPromptWithFilePath(String promptFilePath) {
    Resource resource = resourceLoader.getResource("classpath:" + promptFilePath);
    try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
      return FileCopyUtils.copyToString(reader);
    } catch (IOException e) {
      throw new RuntimeException("프롬프트 파일을 읽는데 실패했습니다 : " + e.getMessage());
    }
  }

  private NaverChatResponseDto getNaverChatResponse(String systemPrompt, String userPrompt) {
    List<ContentPart> systemContent = List.of(new ContentPart("text", systemPrompt, null));
    List<ContentPart> userContent = List.of(new ContentPart("text", userPrompt, null));

    List<MessageDto> messages = List.of(
        new MessageDto("system", systemContent),
        new MessageDto("user", userContent)
    );

    NaverChatRequestDto naverRequest = NaverChatRequestDto.builder()
        .messages(messages)
        .topP(0.8).topK(0).maxTokens(2048).temperature(0.5)
        .stop(List.of())
        .build();

    return webClient.post()
        .uri("/v3/chat-completions/HCX-005")
        .bodyValue(naverRequest)
        .retrieve()
        .onStatus(httpStatus -> httpStatus.isError(),
            clientResponse -> clientResponse.bodyToMono(String.class)
                .flatMap(
                    errorBody -> Mono.error(new RuntimeException("API call failed: " + errorBody))))
        .bodyToMono(NaverChatResponseDto.class)
        .block();
  }

  private NaverChatResponseDto getNaverChatHcx007Response(String systemPrompt, String userPrompt) {
    List<ContentPart> systemContent = List.of(new ContentPart("text", systemPrompt, null));
    List<ContentPart> userContent = List.of(new ContentPart("text", userPrompt, null));

    List<MessageDto> messages = List.of(
        new MessageDto("system", systemContent),
        new MessageDto("user", userContent)
    );
    Map<String, String> effort = new HashMap<>();
    effort.put("effort", "low");
    NaverChatHcx007RequestDto naverRequest = NaverChatHcx007RequestDto.builder()
        .messages(messages)
        .topP(0.8).topK(0).maxCompletionTokens(4096).temperature(0.5).thinking(effort)
        .stop(List.of())
        .build();

    return webClient.post()
        .uri("/v3/chat-completions/HCX-007")
        .bodyValue(naverRequest)
        .retrieve()
        .onStatus(httpStatus -> httpStatus.isError(),
            clientResponse -> clientResponse.bodyToMono(String.class)
                .flatMap(
                    errorBody -> Mono.error(new RuntimeException("API call failed: " + errorBody))))
        .bodyToMono(NaverChatResponseDto.class)
        .block();
  }

  public NaverEmbeddingResponseDto getEmbedding(String queryText) {
    Map<String, String> bodyValue = new HashMap<>();
    bodyValue.put("text", queryText);

    return webClient.post()
        .uri("/v1/api-tools/embedding/v2")
        .bodyValue(bodyValue)
        .retrieve()
        .onStatus(httpStatus -> httpStatus.isError(),
            clientResponse -> clientResponse.bodyToMono(String.class)
                .flatMap(
                    errorBody -> Mono.error(new RuntimeException("API call failed: " + errorBody))))
        .bodyToMono(NaverEmbeddingResponseDto.class)
        .block();
  }

  /**
   * 혼합 응답 문자열에서 JSON(코드펜스 또는 첫 JSON 블록)을 추출합니다. - ```json ... ``` 우선 추출 - 없으면 첫 번째 '[' 또는 '{'부터 괄호
   * 균형이 맞는 지점까지 반환
   */
  private String extractFirstJson(String raw) {
    if (raw == null) {
      return "";
    }
    String s = raw.trim();

    // 1) 코드펜스 우선: ```json ... ```
    Pattern fence = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)```", Pattern.MULTILINE);
    Matcher m = fence.matcher(s);
    if (m.find()) {
      String inside = m.group(1);
      if (inside != null) {
        // 혹시 본문 맨 앞에 'json' 텍스트가 섞여 있으면 제거
        inside = inside.replaceFirst("^\\s*json\\s*", "");
        return inside.trim();
      }
    }

    // 2) 첫 JSON 시작문자부터 괄호 균형이 맞는 지점까지
    int start = -1;
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '[' || c == '{') {
        start = i;
        break;
      }
    }
    if (start < 0) {
      return s; // JSON 시작문자 자체가 없으면 원본문자열 반환(상위에서 실패 처리)
    }

    char open = s.charAt(start);
    char close = (open == '[') ? ']' : '}';
    int depth = 0;
    int end = -1;

    boolean inString = false;
    boolean escape = false;

    for (int i = start; i < s.length(); i++) {
      char c = s.charAt(i);

      if (inString) {
        if (escape) {
          escape = false;
        } else if (c == '\\') {
          escape = true;
        } else if (c == '"') {
          inString = false;
        }
      } else {
        if (c == '"') {
          inString = true;
        } else if (c == open) {
          depth++;
        } else if (c == close) {
          depth--;
          if (depth == 0) {
            end = i;
            break;
          }
        }
      }
    }

    if (end >= start) {
      return s.substring(start, end + 1).trim();
    }
    return s.substring(start).trim(); // 닫힘 문자를 못 찾으면 시작부터 끝까지 반환
  }

  // cleanResponse(...) 는 기존 구현을 그대로 사용
}