package com.moeasy.moeasy.dto.llm.web;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class GoogleSearchResponseDto {
    private List<GoogleSearchItem> items;
}
