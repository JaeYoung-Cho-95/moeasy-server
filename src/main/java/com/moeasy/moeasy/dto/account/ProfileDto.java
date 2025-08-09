package com.moeasy.moeasy.dto.account;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ProfileDto {
    private final String email;
    private final String name;
    private final String profileUrl;

    @Builder
    public ProfileDto(String email, String name, String profileUrl) {
        this.email = email;
        this.name = name;
        this.profileUrl = profileUrl;
    }
}
