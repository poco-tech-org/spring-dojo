package com.example.blog.repository.file;

import com.example.blog.config.S3Config;
import com.example.blog.config.S3Properties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(
        classes = {
                FileRepository.class,
                S3Config.class
        },
        initializers = ConfigDataApplicationContextInitializer.class
)
@EnableConfigurationProperties(S3Properties.class)
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
    @DisplayName("exists > 引数に与えられた imagePathが存在する場合、true を返す")
    void exists_returnTrue() {
        // ## Arrange ##
        var imagePath = "users/1/profile-image.png";
        s3Client.putObject(
                builder -> builder
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

    @Test
    @DisplayName("exists > 引数に与えられた imagePathが存在しない場合、false を返す")
    void exists_returnFalse() {
        // ## Arrange ##
        var imagePath = "users/1/profile-image.png";
        s3Client.putObject(
                builder -> builder
                        .bucket(s3Properties.bucket().profileImages())
                        .key(imagePath)
                        .build(),
                RequestBody.fromString("test")
        );

        // ## Act ##
        var actual = cut.exists("non-existing.png");

        // ## Assert ##
        assertThat(actual).isFalse();
    }
}