package com.example.blog.it;

import com.example.blog.config.S3Properties;
import com.example.blog.model.UserDTO;
import com.example.blog.model.UserProfileImageUploadURLDTO;
import com.example.blog.service.user.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
public class UploadUserProfileImageIT {

    private static final String TEST_USERNAME = "test_username1";
    private static final String TEST_PASSWORD = "password10";
    private static final String DUMMY_SESSION_ID = "session_id_1";
    private static final String SESSION_COOKIE_NAME = "SESSION";
    private static final String TEST_IMAGE_FILE_NAME = "test.png";
    private static final String TEST_IMAGE_FILE_NAME_OTHER_SIZE = "test_otherSize.png";

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
        var registeredUser = register(xsrfToken);

        // ログイン成功
        var sessionId = loginSuccess(xsrfToken);

        // Pre-signed URL の取得
        var uploadUrlDTO = getUserProfileImageUploadURL(
                sessionId,
                MediaType.IMAGE_PNG,
                TEST_IMAGE_FILE_NAME,
                registeredUser.getId()
        );

        // S3へのファイルアップロード
        uploadImage(
                uploadUrlDTO.getImageUploadUrl(),
                MediaType.IMAGE_PNG,
                registeredUser.getId()
        );

        // ファイルパスの登録
        updateUserProfileImage(sessionId, uploadUrlDTO.getImagePath(), xsrfToken);
    }

    @Test
    @DisplayName(
            "Presigned URL 取得時に指定した ContentType と異なる ContentType が指定されたとき、ファイルを S3 にアップロードできない"
    )
    public void contentTypeMismatch() throws IOException {
        //ユーザー作成
        var xsrfToken = getCsrfCookie();
        var registeredUser = register(xsrfToken);

        // ログイン成功
        var sessionId = loginSuccess(xsrfToken);

        // Pre-signed URL の取得
        var uploadUrlDTO = getUserProfileImageUploadURL(
                sessionId,
                MediaType.IMAGE_PNG,
                TEST_IMAGE_FILE_NAME,
                registeredUser.getId()
        );

        // S3へのファイルアップロード
        uploadImageContentTypeMismatch(uploadUrlDTO.getImageUploadUrl(), MediaType.APPLICATION_XML);
    }

    @Test
    @DisplayName(
            "Presigned URL 取得時に指定した ContentLength と異なるファイルは S3 にアップロードできない"
    )
    public void contentLengthMismatch() throws IOException {
        //ユーザー作成
        var xsrfToken = getCsrfCookie();
        var registeredUser = register(xsrfToken);

        // ログイン成功
        var sessionId = loginSuccess(xsrfToken);

        // Pre-signed URL の取得
        var uploadUrlDTO = getUserProfileImageUploadURL(
                sessionId,
                MediaType.IMAGE_PNG,
                TEST_IMAGE_FILE_NAME,
                registeredUser.getId()
        );

        // S3へのファイルアップロード
        uploadImageContentLengthMismatch(
                uploadUrlDTO.getImageUploadUrl(),
                MediaType.IMAGE_PNG,
                TEST_IMAGE_FILE_NAME_OTHER_SIZE
        );
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

    private UserDTO register(String xsrfToken) {
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
        var actualUserDTO = responseSpec
                .expectStatus().isCreated()
                .expectBody(UserDTO.class)
                .returnResult()
                .getResponseBody();

        assertThat(actualUserDTO).isNotNull();

        return actualUserDTO;
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
            MediaType contentType,
            String imageFileName,
            long userId
    ) throws IOException {
        // ## Arrange ##
        var imageResource = new ClassPathResource(TEST_IMAGE_FILE_NAME);
        var imageFile = imageResource.getFile();
        var imageSize = imageFile.length();

        // ## Act ##
        var responseSpec = webTestClient
                .get().uri(uriBuilder -> uriBuilder
                        .path("/users/me/image-upload-url")
                        .queryParam("fileName", imageFileName)
                        .queryParam("contentType", contentType)
                        .queryParam("contentLength", imageSize)
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
        assertThat(actualResponseBody.getImagePath())
                .isEqualTo("users/%d/profile-image".formatted(userId));
        assertThat(actualResponseBody.getImageUploadUrl())
                .hasScheme("http")
                .hasHost("localhost")
                .hasPort(4566)
                .hasPath("/profile-images/users/%d/profile-image".formatted(userId))
                .hasParameter("X-Amz-Expires", "600")
                .hasParameter("X-Amz-Signature")
        ;

        return actualResponseBody;
    }

    private void uploadImage(
            URI imageUploadUrl,
            MediaType contentType,
            long userId
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
                .key("users/%d/profile-image".formatted(userId))
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

    private void uploadImageContentLengthMismatch(
            URI imageUploadUrl,
            MediaType contentType,
            String imageFileName
    ) throws IOException {
        // ## Arrange ##
        var imageResource = new ClassPathResource(imageFileName);
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

    private void updateUserProfileImage(
            String loginSessionCookie,
            String imagePath,
            String xsrfToken
    ) {
        // ## Arrange ##
        var bodyJson = String.format("""
                {
                   "imagePath": "%s"
                }
                """, imagePath);

        // ## Act ##
        var responseSpec = webTestClient
                .put().uri("/users/me/image")
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(SESSION_COOKIE_NAME, loginSessionCookie)
                .cookie("XSRF-TOKEN", xsrfToken)
                .header("X-XSRF-TOKEN", xsrfToken)
                .bodyValue(bodyJson)
                .exchange();

        // ## Assert ##
        responseSpec.expectStatus().isOk();

        // GET /users/me
        var getUserResponseSpec = webTestClient
                .get().uri("/users/me")
                .cookie(SESSION_COOKIE_NAME, loginSessionCookie)
                .exchange();

        getUserResponseSpec
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isNumber()
                .jsonPath("$.username").isEqualTo(TEST_USERNAME)
                .jsonPath("$.imagePath").isEqualTo(imagePath)
                .jsonPath("$.password").doesNotExist();
    }
}
