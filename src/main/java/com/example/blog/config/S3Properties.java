package com.example.blog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("aws.s3")
public record S3Properties(
        String endpoint,
        String region,
        String accessKey,
        String secretKey,
        Bucket bucket
) {

    public record Bucket(
            String profileImages
    ) {

    }
}
