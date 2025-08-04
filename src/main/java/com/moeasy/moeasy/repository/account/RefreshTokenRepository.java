package com.moeasy.moeasy.repository.account;

import com.moeasy.moeasy.domain.account.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByUserEmail(String userEmail);
    Optional<RefreshToken> findByToken(String token);
}
