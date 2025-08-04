package com.moeasy.moeasy.domain.account;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userEmail;

    private String token;

    @Builder
    public RefreshToken(String userEmail, String token) {
        this.userEmail = userEmail;
        this.token = token;
    }

    public void updateToken(String token) {
        this.token = token;
    }
}
