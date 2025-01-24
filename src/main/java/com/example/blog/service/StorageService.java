package com.example.blog.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.time.Duration;
import java.util.Map;

@Service
@Slf4j
public class StorageService {

    public String createUploadURL(
            String fileName,
            String contentType,
            Long contentLength
    ) {
        return createPresignedUrl("profile-images", fileName, Map.of());
    }

    // ref. https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/examples-s3-presign.html#put-presigned-object-part1
    private String createPresignedUrl(
            String bucketName,
            String keyName,
            Map<String, String> metadata
    ) {
        var builder = S3Presigner.builder()
                .serviceConfiguration(
                        S3Configuration
                                .builder()
                                .pathStyleAccessEnabled(true)
                                .build()
                )
                .endpointOverride(
                        URI.create("http://localhost:4566")
                )
                .credentialsProvider(
                        StaticCredentialsProvider
                                .create(
                                        AwsBasicCredentials
                                                .create("test1", "test2")
                                )
                )
                .region(
                        Region.of("ap-northeast-1")
                );

        try (var presigner = builder.build()) {
            var objectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .metadata(metadata)
                    .build();

            var presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))
                    .putObjectRequest(objectRequest)
                    .build();

            return presigner.presignPutObject(presignRequest).url().toString();
        }
    }
}
