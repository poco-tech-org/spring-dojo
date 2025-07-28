package com.example.blog.repository.file;

import com.example.blog.config.S3Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileRepository {

    private final S3Properties s3Properties;
    private final S3Presigner s3Presigner;

    public URI createUploadURL(
            String fileName,
            String contentType,
            long contentLength
    ) {
        return createPresignedUrl(
                s3Properties.bucket().profileImages(),
                fileName,
                contentType,
                contentLength,
                Map.of()
        );
    }

    // ref. https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/examples-s3-presign.html#put-presigned-object-part1
    /* Create a presigned URL to use in a subsequent PUT request */
    private URI createPresignedUrl(
            String bucketName,
            String keyName,
            String contentType,
            long contentLength,
            Map<String, String> metadata
    ) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .contentType(contentType)
                .contentLength(contentLength)
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

        try {
            return presignedRequest.url().toURI();
        } catch (URISyntaxException e) {
            log.error("Failed to convert URL [{}] to URI", presignedRequest.url(), e);
            throw new IllegalStateException("Failed to convert presigned URL to URI", e);
        }
    }
}
