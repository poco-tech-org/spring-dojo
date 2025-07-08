package com.example.blog.service;

import com.example.blog.config.S3Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.time.Duration;
import java.util.Map;

@Service
@Slf4j
public class StorageService {

    private final S3Properties s3Properties;
    private final S3Presigner s3Presigner;

    public StorageService(S3Properties s3Properties) {
        this.s3Properties = s3Properties;
        this.s3Presigner = S3Presigner.builder()
                .serviceConfiguration(
                        S3Configuration.builder()
                                .pathStyleAccessEnabled(true)
                                .build())
                .endpointOverride(
                        URI.create(s3Properties.endpoint())
                )
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        s3Properties.accessKey(),
                                        s3Properties.secretKey()
                                )
                        )
                )
                .region(
                        Region.of(s3Properties.region())
                )
                .build();
    }

    public String createUploadURL(
            String fileName,
            String contentType,
            Long contentLength
    ) {
        return createPresignedUrl(
                s3Properties.bucket().profileImages(),
                "test-key",
                Map.of()
        );
    }

    // ref. https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/examples-s3-presign.html#put-presigned-object-part1
    /* Create a presigned URL to use in a subsequent PUT request */
    private String createPresignedUrl(
            String bucketName,
            String keyName,
            Map<String, String> metadata
    ) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .metadata(metadata)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))  // The URL expires in 10 minutes.
                .putObjectRequest(objectRequest)
                .build();


        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        String myURL = presignedRequest.url().toString();
        log.info("Presigned URL to upload a file to: [{}]", myURL);
        log.info("HTTP method: [{}]", presignedRequest.httpRequest().method());

        return presignedRequest.url().toExternalForm();
    }
}
