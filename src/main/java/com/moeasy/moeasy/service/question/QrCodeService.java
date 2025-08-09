package com.moeasy.moeasy.service.question;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.moeasy.moeasy.domain.question.Question;
import com.moeasy.moeasy.dto.quesiton.VerifyQrCodeDto;
import com.moeasy.moeasy.repository.question.QuestionRepository;
import com.moeasy.moeasy.service.aws.AwsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QrCodeService {

    @Value("${spring.application.security.url-signer.secret-key}")
    private String secretKey;

    @Value("${spring.application.security.url-signer.algorithm}")
    private String algorithm;

    @Autowired private final AwsService awsService;
    @Autowired private final QuestionRepository questionRepository;

    public Map<String, String> getQrCodeS3Url(Long questionId) throws WriterException, IOException {
        long expires = Instant.now().plus(7, ChronoUnit.DAYS).toEpochMilli();
        String questionIdStr = questionId.toString();

        String dataToSign = "expires=" + expires + "&id=" + questionIdStr;
        String signature = createSignature(dataToSign);

        String url = "https://mo-easy.com/question/" + questionId.toString() + "?expires=" + expires + "&signature=" + signature;
        String s3Url = saveQrCodeToS3(url, questionId.toString());

        Map<String, String> response = new HashMap<>();
        response.put("url", url);
        response.put("qrCode", s3Url);

        return response;
    }

    public BufferedImage makeQrCodeBufferedImage(String url) throws WriterException  {
        int width = 400;
        int height = 400;

        BitMatrix matrix = new MultiFormatWriter().encode(url, BarcodeFormat.QR_CODE, width, height);
        return MatrixToImageWriter.toBufferedImage(matrix);
    }

    public String saveQrCodeToS3(String url, String questionId) throws WriterException, IOException {
        BufferedImage qrImage = makeQrCodeBufferedImage(url);
        return awsService.upload(qrImage, questionId, "qr_code");
    }

    private String createSignature(String data) {
        try {
            Mac mac = Mac.getInstance(algorithm);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), algorithm);
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hmacBytes);

        } catch (Exception e) {
            throw new RuntimeException("Failed to create signature : " + e);
        }
    }
    
    public Optional<Question> verifyQrCode(VerifyQrCodeDto verifyQrCodeDto) {
        long expires = Long.parseLong(verifyQrCodeDto.getExpires());
        if (Instant.now().toEpochMilli() > expires) {
            return Optional.empty();
        }

        String dataToVerify = "expires=" + verifyQrCodeDto.getExpires() + "&id=" + verifyQrCodeDto.getQuestionId();
        String expectedSignature = createSignature(dataToVerify);

        boolean isVerifed = MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), verifyQrCodeDto.getSignature().getBytes(StandardCharsets.UTF_8));

        if (isVerifed) {
            return questionRepository.findById(Long.parseLong(verifyQrCodeDto.getQuestionId()));
        }
        return Optional.empty();
    }
}