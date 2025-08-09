package com.moeasy.moeasy.service.aws;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;


@Slf4j
@RequiredArgsConstructor
@Service
public class AwsService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.qr_bucket}")
    private String qr_bucket;

    @Value("${cloud.aws.s3.profile_bucket}")
    private String profile_bucket;


    public String upload(BufferedImage image, String id, String bucket) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        byte[] buffer = os.toByteArray();

        String fileName = bucket.equals("qr_code") ?
                id + "/" + "qr_code.png" :
                id + "/" + "profile.png";

        // 1. PutObjectRequest 생성 (빌더 사용)
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket.equals("qr_code") ? qr_bucket : profile_bucket)
                .key(fileName)
                .contentType("image/png")
                .contentLength((long) buffer.length)
                .build();

        // 2. 객체 업로드
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(buffer));

        // 3. Presigned URL 생성
        return generatePresignedUrl(fileName, bucket);
    }

    public String generatePresignedUrl(String fileName, String bucket) {
        // GetObjectRequest 생성
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket.equals("qr_code") ? qr_bucket : profile_bucket)
                .key(fileName)
                .build();

        // Presigned URL 생성 요청
        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(1)) // URL 유효 시간: 1시간
                .getObjectRequest(getObjectRequest)
                .build();

        // URL 서명
        PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(getObjectPresignRequest);

        return presignedGetObjectRequest.url().toString();
    }
}