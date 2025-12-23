package com.example.blog.repository.file;

import com.example.blog.config.S3PresignerConfig;
import com.example.blog.config.S3Properties;
import com.example.blog.config.TestS3ClientConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(
        classes = {
                FileRepository.class,
                S3PresignerConfig.class
        },
        initializers = ConfigDataApplicationContextInitializer.class
)
@EnableConfigurationProperties(S3Properties.class)
@Import(TestS3ClientConfig.class)
class FileRepositoryTest {

    @Autowired
    private FileRepository cut;
    @Autowired
    private S3Properties s3Properties;
    @Autowired
    private S3Client s3Client;

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
                .hasPath("/" + s3Properties.bucket().profileImages() + "/test.png")
                .hasParameter("X-Amz-Expires", "600")
                .hasParameter("X-Amz-SignedHeaders", "content-length;content-type;host")
                .hasParameter("X-Amz-Signature")
        ;
    }

    @Test
    @DisplayName("exists > 引数に与えられた imagePath が存在する場合、true を返す")
    void exists_returnTrue() {
        // ## Arrange ##
        var imagePath = "users/1/profile-image";
        s3Client.putObject(builder -> builder
                        .bucket(s3Properties.bucket().profileImages())
                        .key(imagePath)
                        .build(),
                RequestBody.fromString("test")
        );

        // ## Act ##
        var actual = cut.exists(imagePath);

        // ## Assert ##
        assertThat(actual).isTrue();
    }

    @ParameterizedTest
    @DisplayName("exists > 引数に与えられた imagePath が存在しない場合、false を返す")
    @NullSource
    @ValueSource(strings = {"", "non-existing-image-path"})
    void exists_returnFalse(String inputImagePath) {
        // ## Arrange ##
        var imagePath = "users/1/profile-image";
        s3Client.putObject(builder -> builder
                        .bucket(s3Properties.bucket().profileImages())
                        .key(imagePath)
                        .build(),
                RequestBody.fromString("test")
        );

        // ## Act ##
        var actual = cut.exists(inputImagePath);

        // ## Assert ##
        assertThat(actual).isFalse();
    }
}