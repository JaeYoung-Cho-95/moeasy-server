package com.moeasy.moeasy.service.question;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.moeasy.moeasy.config.response.custom.CustomErrorException;
import com.moeasy.moeasy.domain.question.Question;
import com.moeasy.moeasy.dto.onboarding.response.QrCodeResponseDto;
import com.moeasy.moeasy.dto.quesiton.VerifyQrCodeDto;
import com.moeasy.moeasy.repository.question.QuestionRepository;
import com.moeasy.moeasy.service.aws.AwsService;
import jakarta.persistence.EntityNotFoundException;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QrCodeService {

  @Value("${spring.application.security.url-signer.secret-key}")
  private String secretKey;

  @Value("${spring.application.security.url-signer.algorithm}")
  private String algorithm;

  // qr 코드의 유효 기간은 7일 입니다.
  private final long QR_CODE_EXPIRED_TIME = 7;

  private final AwsService awsService;
  private final QuestionRepository questionRepository;

  @Transactional
  public QrCodeResponseDto makeQrCodeS3Url(Long questionId) {
    long expires = Instant.now().plus(QR_CODE_EXPIRED_TIME, ChronoUnit.DAYS).toEpochMilli();
    String questionIdStr = questionId.toString();

    String dataToSign = "expires=" + expires + "&id=" + questionIdStr;
    String signature = createSignature(dataToSign);

    String url = "https://mo-easy.com/question/" + questionId.toString() + "?expires=" + expires
        + "&signature=" + signature;
    String s3Url = saveQrCodeToS3(url, questionId.toString());

    saveUrlInQrCode(questionId, url);
    return QrCodeResponseDto.from(url, s3Url);
  }

  public BufferedImage makeQrCodeBufferedImage(String url) {
    int width = 400;
    int height = 400;

    try {
      BitMatrix matrix = new MultiFormatWriter().encode(url, BarcodeFormat.QR_CODE, width, height);
      return MatrixToImageWriter.toBufferedImage(matrix);
    } catch (WriterException e) {
      throw CustomErrorException.from(HttpStatus.INTERNAL_SERVER_ERROR, "qr 코드 제작 중 에러가 발생했습니다.");
    }
  }

  public String saveQrCodeToS3(String url, String questionId) {
    BufferedImage qrImage = makeQrCodeBufferedImage(url);
    return awsService.upload(qrImage, questionId, "qr_code");
  }

  private String createSignature(String data) {
    try {
      Mac mac = Mac.getInstance(algorithm);
      SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8),
          algorithm);
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

    String dataToVerify =
        "expires=" + verifyQrCodeDto.getExpires() + "&id=" + verifyQrCodeDto.getQuestionId();
    String expectedSignature = createSignature(dataToVerify);

    boolean isVerifed = MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8),
        verifyQrCodeDto.getSignature().getBytes(StandardCharsets.UTF_8));

    if (isVerifed) {
      return questionRepository.findById(Long.parseLong(verifyQrCodeDto.getQuestionId()));
    }
    return Optional.empty();
  }

  public void saveUrlInQrCode(Long questiondId, String url) {
    Question question = questionRepository.findById(questiondId)
        .orElseThrow(() -> new EntityNotFoundException("Question not found : " + questiondId));

    question.updateUrlInQrCode(url);
  }
}