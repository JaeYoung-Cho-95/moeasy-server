package com.moeasy.moeasy.service.llm.web;

import com.moeasy.moeasy.dto.llm.web.GoogleSearchItem;
import com.moeasy.moeasy.dto.llm.web.GoogleSearchResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoogleWebSearchService implements WebSearchService {

    @Value("${google.web-search.secret-key")
    private String secretKey;

    @Value("${google.web-search.engine-id")
    private String engineId;

    private final WebClient webClient = WebClient.create("https://www.googleapis.com/customsearch/v1");

    @Override
    public List<String> search(String searchWord) {
        GoogleSearchResponseDto response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("key", secretKey)
                        .queryParam("cs", engineId)
                        .queryParam("q", searchWord)
                        .queryParam("num", 3)
                        .build())
                .retrieve()
                .bodyToMono(GoogleSearchResponseDto.class)
                .block();

        if (response == null || response.getItems() == null) {
            return Collections.emptyList();
        }

        return response.getItems().stream()
                .map(GoogleSearchItem::getSnippet)
                .collect(Collectors.toList());
    }
}
