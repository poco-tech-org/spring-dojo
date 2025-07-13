package com.example.blog.it;

import com.example.blog.config.S3Properties;
import com.example.blog.config.TestS3ClientConfig;
import com.example.blog.model.UserProfileImageUploadURLDTO;
import com.example.blog.service.user.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestS3ClientConfig.class)
public class UploadUserProfileImageIT {

    private static final String TEST_USERNAME = "test_username1";
    private static final String TEST_PASSWORD = "password10";
    private static final String DUMMY_SESSION_ID = "session_id_1";
    private static final String SESSION_COOKIE_NAME = "SESSION";
    private static final String TEST_IMAGE_FILE_NAME = "test.png";

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private UserService userService;
    @Autowired
    private S3Properties s3Properties;
    @Autowired
    private S3Client testS3Client;

    @BeforeEach
    public void beforeEach() {
        userService.delete(TEST_USERNAME);
        deleteImage(TEST_IMAGE_FILE_NAME);
    }

    @AfterEach
    public void afterEach() {
        userService.delete(TEST_USERNAME);
        deleteImage(TEST_IMAGE_FILE_NAME);
    }

    private void deleteImage(String fileName) {
        testS3Client.deleteObject(builder -> builder
                .bucket(s3Properties.bucket().profileImages())
                .key(fileName)
                .build()
        );
    }

    @Test
    public void integrationTest() throws IOException {
        //ユーザー作成
        var xsrfToken = getCsrfCookie();
        register(xsrfToken);

        // ログイン成功
        var sessionId = loginSuccess(xsrfToken);

        // Pre-signed URL の取得
        var uploadUrlDTO = getUserProfileImageUploadURL(sessionId, MediaType.IMAGE_PNG);

        // S3へのファイルアップロード
        uploadImage(uploadUrlDTO.getImageUploadUrl(), MediaType.IMAGE_PNG);

        // ファイルパスの登録
    }

    @Test
    @DisplayName(
            "Presigned URL 取得時に指定した ContentType と異なる ContentType が指定されたとき、ファイルを S3 にアップロードできない"
    )
    public void contentTypeMismatch() throws IOException {
        //ユーザー作成
        var xsrfToken = getCsrfCookie();
        register(xsrfToken);

        // ログイン成功
        var sessionId = loginSuccess(xsrfToken);

        // Pre-signed URL の取得
        var uploadUrlDTO = getUserProfileImageUploadURL(sessionId, MediaType.IMAGE_PNG);

        // S3へのファイルアップロード
        uploadImageContentTypeMismatch(uploadUrlDTO.getImageUploadUrl(), MediaType.APPLICATION_XML);

    }

    private String getCsrfCookie() {
        // ## Arrange ##

        // ## Act ##
        var responseSpec = webTestClient.get().uri("/csrf-cookie").exchange();

        // ## Assert ##
        var response = responseSpec.returnResult(String.class);
        var xsrfTokenOpt = Optional.ofNullable(response.getResponseCookies().getFirst("XSRF-TOKEN"));

        responseSpec.expectStatus().isNoContent();
        assertThat(xsrfTokenOpt)
                .isPresent()
                .hasValueSatisfying(xsrfTokenCookie ->
                        assertThat(xsrfTokenCookie.getValue()).isNotBlank()
                );

        return xsrfTokenOpt.get().getValue();
    }

    private void register(String xsrfToken) {
        // ## Arrange ##
        var bodyJson = String.format("""
                {
                  "username": "%s",
                  "password": "%s"
                }
                """, TEST_USERNAME, TEST_PASSWORD);

        // ## Act ##
        var responseSpec = webTestClient
                .post().uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .cookie("XSRF-TOKEN", xsrfToken)
                .header("X-XSRF-TOKEN", xsrfToken)
                .bodyValue(bodyJson)
                .exchange();

        // ## Assert ##
        responseSpec.expectStatus().isCreated();
    }

    private String loginSuccess(String xsrfToken) {
        // ## Arrange ##
        var bodyJson = String.format("""
                {
                  "username": "%s",
                  "password": "%s"
                }
                """, TEST_USERNAME, TEST_PASSWORD);

        // ## Act ##
        var responseSpec = webTestClient
                .post().uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .cookie("XSRF-TOKEN", xsrfToken)
                .cookie(SESSION_COOKIE_NAME, DUMMY_SESSION_ID)
                .header("X-XSRF-TOKEN", xsrfToken)
                .bodyValue(bodyJson)
                .exchange();

        // ## Assert ##
        var response = responseSpec.returnResult(String.class);
        var sessionIdOpt = Optional.ofNullable(response.getResponseCookies().getFirst(SESSION_COOKIE_NAME));
        assertThat(sessionIdOpt)
                .isPresent()
                .hasValueSatisfying(sessionId -> assertThat(sessionId.getValue()).isNotBlank());

        return sessionIdOpt.get().getValue();
    }

    private UserProfileImageUploadURLDTO getUserProfileImageUploadURL(
            String loginSessionCookie,
            MediaType contentType
    ) {
        // ## Act ##
        var responseSpec = webTestClient
                .get().uri(uriBuilder -> uriBuilder
                        .path("/users/me/image-upload-url")
                        .queryParam("fileName", TEST_IMAGE_FILE_NAME)
                        .queryParam("contentType", contentType)
                        .queryParam("contentLength", 104892)
                        .build()
                )
                .cookie(SESSION_COOKIE_NAME, loginSessionCookie)
                .exchange();

        // ## Assert ##
        var actualResponseBody = responseSpec
                .expectStatus().isOk()
                .expectBody(UserProfileImageUploadURLDTO.class)
                .returnResult()
                .getResponseBody();

        assertThat(actualResponseBody).isNotNull();
        assertThat(actualResponseBody.getImagePath()).isNotBlank();
        assertThat(actualResponseBody.getImageUploadUrl())
                .hasScheme("http")
                .hasHost("localhost")
                .hasPort(4566)
                .hasPath("/profile-images/" + TEST_IMAGE_FILE_NAME)
                .hasParameter("X-Amz-Expires", "600")
                .hasParameter("X-Amz-Signature")
        ;

        return actualResponseBody;
    }

    private void uploadImage(
            URI imageUploadUrl,
            MediaType contentType
    ) throws IOException {
        // ## Arrange ##
        var imageResource = new ClassPathResource(TEST_IMAGE_FILE_NAME);
        var imageFile = imageResource.getFile();
        var imageBytes = Files.readAllBytes(imageFile.toPath());

        // ## Act ##
        var responseSpec = webTestClient
                .put().uri(imageUploadUrl)
                .contentType(contentType)
                .bodyValue(imageBytes)
                .exchange();

        // ## Assert ##
        responseSpec.expectStatus().isOk();

        // S3 にファイルがアップロードされているか
        var request = GetObjectRequest.builder()
                .bucket(s3Properties.bucket().profileImages())
                .key(TEST_IMAGE_FILE_NAME)
                .build();
        var response = testS3Client.getObject(request);
        var actualImages = response.readAllBytes();
        assertThat(actualImages).isEqualTo(imageBytes);
    }


    private void uploadImageContentTypeMismatch(
            URI imageUploadUrl,
            MediaType contentType
    ) throws IOException {
        // ## Arrange ##
        var imageResource = new ClassPathResource(TEST_IMAGE_FILE_NAME);
        var imageFile = imageResource.getFile();
        var imageBytes = Files.readAllBytes(imageFile.toPath());

        // ## Act ##
        var responseSpec = webTestClient
                .put().uri(imageUploadUrl)
                .contentType(contentType)
                .bodyValue(imageBytes)
                .exchange();

        // ## Assert ##
        responseSpec.expectStatus().isForbidden();

        // S3 にファイルがアップロードされていないことを確認する
        var request = HeadObjectRequest.builder()
                .bucket(s3Properties.bucket().profileImages())
                .key(TEST_IMAGE_FILE_NAME)
                .build();
        assertThatThrownBy(() -> testS3Client.headObject(request))
                .isInstanceOf(NoSuchKeyException.class);
    }
}
