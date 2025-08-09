package com.moeasy.moeasy.domain.question;

import com.moeasy.moeasy.domain.account.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;


@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Question {
    @Id
    @GeneratedValue
    @Column(name = "question_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String title;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private String content;

    private LocalDateTime createdTime;
    private LocalDateTime expirationTime;
    private Boolean expired;
    private Integer count;


    @Builder
    private Question(Member member, String title, String content) {
        this.member = member;
        this.title = title;
        this.content = content;
    }

    @PrePersist
    public void onPrePersist() {
        this.createdTime = LocalDateTime.now();
        this.expirationTime = this.createdTime.plusWeeks(1);
        this.expired = true;
        this.count = 0;
    }
}
