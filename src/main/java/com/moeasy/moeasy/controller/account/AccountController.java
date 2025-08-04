package com.moeasy.moeasy.controller.account;

import com.moeasy.moeasy.common.SuccessApiResponseDto;
import com.moeasy.moeasy.dto.account.KaKaoDto;
import com.moeasy.moeasy.service.account.KakaoService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("account")
public class AccountController {

    private final KakaoService kakaoService;

    @GetMapping("/callback")
    public ResponseEntity<SuccessApiResponseDto<Map<String, String>>> callback(HttpServletRequest request) throws Exception {
        KaKaoDto kakaoInfo = kakaoService.getKakaoInfo(request.getParameter("code"));
        System.out.println(kakaoInfo);

        Map<String, String> data = new HashMap<>();
        data.put("access_token", "sample_access_token");
        data.put("refresh_token", "sample_refresh_token");

        return ResponseEntity.ok()
                .body(
                        SuccessApiResponseDto.success(
                                200, "login success", data
                        )
                );
    }
}
