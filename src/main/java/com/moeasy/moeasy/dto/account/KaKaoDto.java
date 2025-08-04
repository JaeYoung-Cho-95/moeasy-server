package com.moeasy.moeasy.dto.account;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class KaKaoDto {

    private long id;
    private String email;
    private String nickname;
}
