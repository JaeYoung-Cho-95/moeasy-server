package com.moeasy.moeasy.service.account;

import com.moeasy.moeasy.domain.account.Member;
import com.moeasy.moeasy.repository.account.MemberRepository;
import com.moeasy.moeasy.repository.account.RefreshTokenRepository;
import com.moeasy.moeasy.service.aws.AwsService;
import jakarta.persistence.EntityNotFoundException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final AwsService awsService;


  public Member findOrCreateMember(String email, String username) {
    Optional<Member> findByEmailMember = memberRepository.findByEmail(email);

    if (findByEmailMember.isPresent()) {
      return findByEmailMember.get();
    }

    Member newMember = new Member();
    newMember.setEmail(email);
    newMember.setUsername(username);
    memberRepository.save(newMember);

    awsService.upload(loadRandomDefaultProfileImage(), String.valueOf(newMember.getId()),
        "profile");
    newMember.setProfileUrl(String.valueOf(newMember.getId()) + "/profile.png");

    return newMember;
  }

  public void deleteMember(Long id) {
    Member member = memberRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Id : " + id + " 에 해당하는 회원을 찾을 수 없습니다."));

    memberRepository.removeById(member.getId());
    refreshTokenRepository.findByUserEmail(member.getEmail())
        .ifPresent(refreshTokenRepository::delete);
  }

  private BufferedImage loadRandomDefaultProfileImage() {
    String basePath = "userProfiles/";
    String[] candidates = {"profile1.png", "profile2.png", "profile3.png"};
    int idx = ThreadLocalRandom.current().nextInt(candidates.length);
    ClassPathResource resource = new ClassPathResource(basePath + candidates[idx]);

    try (InputStream is = resource.getInputStream()) {
      return ImageIO.read(is);
    } catch (IOException e) {
      throw new IllegalStateException("기본 프로필 이미지를 불러올 수 없습니다: " + resource.getPath(), e);
    }
  }

}
