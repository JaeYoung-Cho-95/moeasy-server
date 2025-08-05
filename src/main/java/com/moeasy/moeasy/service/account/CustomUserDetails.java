package com.moeasy.moeasy.service.account;


import com.moeasy.moeasy.domain.account.Member;
import com.moeasy.moeasy.domain.question.Question;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Member member;

    public CustomUserDetails(Member member) {
        this.member = member;
    }

    @Override
    public String getUsername() {
        return member.getEmail();
    }

    @Override
    public String getPassword() {
        return null;
    }

    public String getName() {
        return member.getUsername();
    }

    public String getEmail() {
        return member.getEmail();
    }

    public Long getId() {
        return member.getId();
    }

    public List<Question> getQuestions() {
        return member.getQuestions();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 사용자 권한을 반환하는 로직 (현재는 빈 리스트)
        return Collections.emptyList();
    }

    // 계정의 만료, 잠김, 활성화 여부 등은 필요에 따라 구현합니다.
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
