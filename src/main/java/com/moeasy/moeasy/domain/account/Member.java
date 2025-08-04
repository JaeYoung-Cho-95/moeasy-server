package com.moeasy.moeasy.domain.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
public class Member {

    @Id @GeneratedValue
    @Column(name="member_id")
    private Long id;

    private String username;
    private String email;
}
