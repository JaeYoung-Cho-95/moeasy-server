package com.moeasy.moeasy.service.aws;

import com.moeasy.moeasy.config.response.custom.CustomErrorException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;


@Slf4j
@RequiredArgsConstructor
@Service
public class AwsService {

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;

  @Value("${cloud.aws.s3.qr_bucket}")
  private String QR_BUCKET;

  @Value("${cloud.aws.s3.profile_bucket}")
  private String PROFILE_BUCKET;

  private final Duration PRESIGNED_URL_EXPIRED_TIME = Duration.ofHours(1);

  public String upload(BufferedImage image, String id, String bucket) {
    encodingImage(image);
    String fileName = createFileName(bucket, id);
    byte[] buffer = makeBuffer();
    s3Client.putObject(
        buildPubObjectRequest(bucket, fileName, buffer),
        RequestBody.fromBytes(buffer)
    );
    return generatePresignedUrl(fileName, bucket);
  }

  private void encodingImage(BufferedImage image) {
    try {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      ImageIO.write(image, "png", os);
    } catch (IOException e) {
      throw CustomErrorException.from(HttpStatus.INTERNAL_SERVER_ERROR, "qr image 인코딩 중 에러 발생");
    }

  }

  public String generatePresignedUrl(String fileName, String bucket) {
    GetObjectRequest getObjectRequest = buildGetObjectRequest(fileName, bucket);

    GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
        .signatureDuration(PRESIGNED_URL_EXPIRED_TIME) // URL 유효 시간: 1시간
        .getObjectRequest(getObjectRequest)
        .build();

    PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(
        getObjectPresignRequest);

    return presignedGetObjectRequest.url().toString();
  }

  private String createFileName(String bucket, String id) {
    return bucket.equals("qr_code") ?
        id + "/" + "qr_code.png" :
        id + "/" + "profile.png";
  }

  private byte[] makeBuffer() {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    return os.toByteArray();
  }

  private PutObjectRequest buildPubObjectRequest(String bucket, String fileName, byte[] buffer) {
    return PutObjectRequest.builder()
        .bucket(bucket.equals("qr_code") ? QR_BUCKET : PROFILE_BUCKET)
        .key(fileName)
        .contentType("image/png")
        .contentLength((long) buffer.length)
        .build();
  }

  private GetObjectRequest buildGetObjectRequest(String fileName, String bucket) {
    return GetObjectRequest.builder()
        .bucket(bucket.equals("qr_code") ? QR_BUCKET : PROFILE_BUCKET)
        .key(fileName)
        .build();
  }
}