package com.example.blog.repository.file;

import com.example.blog.config.S3Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileRepository {

    private static final int SIGNATURE_DURATION_MINUTES = 10;
    private final S3Properties s3Properties;
    private final S3Presigner s3Presigner;

    public URI createUploadURL(
            String fileName,
            String contentType,
            long contentLength
    ) {
        var objectRequest = PutObjectRequest.builder()
                .bucket(s3Properties.bucket().profileImages())
                .key(fileName)
                .contentType(contentType)
                .contentLength(contentLength)
                .build();

        var presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(SIGNATURE_DURATION_MINUTES))
                .putObjectRequest(objectRequest)
                .build();

        var presignedRequest = s3Presigner.presignPutObject(presignRequest);

        try {
            return presignedRequest.url().toURI();
        } catch (URISyntaxException e) {
            log.error("Failed to convert URL [{}] to URI", presignedRequest.url(), e);
            throw new IllegalStateException("Failed to convert presigned URL to URI", e);
        }
    }
}
