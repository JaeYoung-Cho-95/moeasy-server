package com.moeasy.moeasy.service.account;

import com.moeasy.moeasy.domain.account.Member;
import com.moeasy.moeasy.repository.account.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    @Autowired
    private final MemberRepository memberRepository;

    public Optional<Member> findMember(String email) {
        return memberRepository.findByEmail(email);
    }

    public Member findOrCreateMember(String email, String username) {
        return memberRepository.findByEmail(email)
                .orElseGet(() -> {
                    Member newMember = new Member();
                    newMember.setEmail(email);
                    newMember.setUsername(username);
                    return memberRepository.save(newMember);
                });
    }

    public void deleteMember(Long id) {
        if (!memberRepository.existsById(id)) {
            throw new EntityNotFoundException("Id : " + id + " 에 해당하는 회원을 찾을 수 없습니다.");
        }
        memberRepository.removeById(id);
    }
}
