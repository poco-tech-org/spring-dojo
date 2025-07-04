package com.example.blog.service;

import com.example.blog.config.S3Properties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(
        classes = StorageService.class,
        initializers = ConfigDataApplicationContextInitializer.class
)
@EnableConfigurationProperties(S3Properties.class)
class StorageServiceTest {

    @Autowired
    private StorageService cut;

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
        assertThat(actual).isNotBlank();
    }
}