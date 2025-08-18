package com.moeasy.moeasy.service.account;

import com.moeasy.moeasy.domain.account.Member;
import com.moeasy.moeasy.dto.account.AppleLoginDto;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class AppleService {

    @Autowired
    private final MemberService memberService;

    @Value("${apple.client.id}")
    private String clientId;

    @Value("${apple.key.id}")
    private String keyId;

    @Value("${apple.team.id}")
    private String teamId;

    @Value("${apple.key.path}")
    private String keyPath;         // .p8 파일 경로

    private static final String APPLE_TOKEN_URL = "https://appleid.apple.com/auth/token";

    public String verifyAuthorizationCode(AppleLoginDto dto) throws Exception {
        String authorizationCode = dto.getAuthCode();
        String clientSecret = generateClientSecret();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("code", authorizationCode);
        body.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                APPLE_TOKEN_URL,
                HttpMethod.POST,
                request,
                String.class
        );

        JSONParser jsonParser = new JSONParser();
        JSONObject json = (JSONObject) jsonParser.parse(response.getBody());
        String idToken = (String) json.get("id_token");

        if (idToken == null) {
            throw new RuntimeException("Apple id_token is null");
        }

        String[] parts = idToken.split("\\.");
        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
        JSONObject payload = (JSONObject) jsonParser.parse(payloadJson);

        String email = (String) payload.get("email");
//        String sub = (String) payload.get("sub");
        String emailVerified = payload.containsKey("email_verified") ? payload.get("email_verified").toString() : "false";

        if (!"true".equalsIgnoreCase(emailVerified)) {
            throw new RuntimeException("Email not verified by Apple");
        }

        Member member = memberService.findOrCreateMember(email, dto.makeFullName());

        HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        HttpSession session = req.getSession();
        session.setAttribute("member", member);

        return email;
    }

    private String generateClientSecret() throws Exception {
        PrivateKey privateKey = loadPrivateKey();

        long now = System.currentTimeMillis() / 1000;
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(teamId)
                .issueTime(new Date(now * 1000))
                .expirationTime(new Date((now + 86400 * 180) * 1000)) // 6개월
                .audience("https://appleid.apple.com")
                .subject(clientId)
                .build();

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256)
                .keyID(keyId)
                .type(JOSEObjectType.JWT)
                .build();

        SignedJWT signedJWT = new SignedJWT(header, claims);
        signedJWT.sign(new ECDSASigner((ECPrivateKey) privateKey));

        return signedJWT.serialize();
    }

    private PrivateKey loadPrivateKey() throws Exception {
        String privateKeyPem = Files.readString(Paths.get(keyPath))
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] pkcs8EncodedBytes = Base64.getDecoder().decode(privateKeyPem);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8EncodedBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return keyFactory.generatePrivate(keySpec);
    }
}

/*  /auth/token 예시
    {
        "access_token": "adg61...67Or9",
        "token_type": "Bearer",
        "expires_in": 3600,
        "refresh_token": "rca7...lABoQ",
        "id_token": "eyJra...96sZg"
    }
 */