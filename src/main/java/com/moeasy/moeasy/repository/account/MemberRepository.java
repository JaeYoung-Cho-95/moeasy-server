package com.moeasy.moeasy.repository.account;

import com.moeasy.moeasy.domain.account.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Member save(Member member);

    Optional<Member> findByEmail(String email);

    void removeById(Long id);
}
