package com.example.blog.service;

import com.example.blog.config.S3PresignerConfig;
import com.example.blog.config.S3Properties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(
        classes = {
                StorageService.class,
                S3PresignerConfig.class
        },
        initializers = ConfigDataApplicationContextInitializer.class
)
@EnableConfigurationProperties(S3Properties.class)
class StorageServiceTest {

    @Autowired
    private StorageService cut;
    @Autowired
    private S3Properties s3Properties;

    @Test
    void test() {
        assertThat(cut).isNotNull();
    }

    @Test
    @DisplayName("createUploadURL")
    void createUploadURL_success() {
        // ## Arrange ##

        // ## Act ##
        var actual = cut.createUploadURL("test.png", "image/png", 111L);

        // ## Assert ##
        assertThat(actual)
                .contains(s3Properties.bucket().profileImages() + "/test.png")
                .contains("X-Amz-Expires=600")
                .contains("X-Amz-SignedHeaders=content-type%3Bhost")
                .contains("X-Amz-Signature")
        ;
    }
}