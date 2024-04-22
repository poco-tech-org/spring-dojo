package com.example.blog.it;

import com.example.blog.service.user.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RegistrationAndLoginIT {

    private static final String TEST_USERNAME = "user1";
    private static final String TEST_PASSWORD = "password1";
    private static final String DUMMY_SESSION_ID = "session_id_1";

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private UserService userService;

    @BeforeEach
    public void beforeEach() {
        userService.delete(TEST_USERNAME);
    }

    @AfterEach
    public void afterEach() {
        userService.delete(TEST_USERNAME);
    }

    @Test
    public void integrationTest() {
        //ユーザー作成
        var xsrfToken = getRoot();
        register(xsrfToken);

        // ログイン失敗
        // Cookie に XSRF-TOKEN がない
        loginFailure_NoXSRFTokenInCookie(xsrfToken);

        // ヘッダーに X-XSRF-TOKEN がない
        loginFailure_NoXXSRFTokenInHeader(xsrfToken);

        // Cookie の XSRF-TOKEN とヘッダーの X-XSRF-TOKEN の値が異なる
        loginFailure_DifferentToken(xsrfToken);

        // ユーザー名が存在しない
        loginFailure_GivenUsernameDoesNotExistInDatabase(xsrfToken);
        //パスワードがデータベースに保存されているパスワードと違う

        //ログイン成功
        loginSuccess(xsrfToken);
        //ユーザー名がデータベースに存在する
        //パスワードがデータベースに保存されているパスワードと違う
        //Cookie の XSRF-TOKEN とヘッダーの X-XSRF-TOKEN の値が一致する
        //→ 200 OK が返る
        //→ レスポンスに Set-Cookie: JSESSIONID が返ってくる
    }

    private String getRoot() {
        // ## Arrange ##

        // ## Act ##
        var responseSpec = webTestClient.get().uri("/").exchange();

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

    private void loginSuccess(String xsrfToken) {
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
                .cookie("JSESSIONID", DUMMY_SESSION_ID)
                .header("X-XSRF-TOKEN", xsrfToken)
                .bodyValue(bodyJson)
                .exchange();

        // ## Assert ##
        responseSpec
                .expectStatus().isOk()
                .expectCookie().value("JSESSIONID", v -> assertThat(v)
                        .isNotBlank()
                        .isNotEqualTo(DUMMY_SESSION_ID)
                );
    }

    private void loginFailure_NoXSRFTokenInCookie(String xsrfToken) {
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
                /*
                .cookie("XSRF-TOKEN", xsrfToken)
                */
                .cookie("JSESSIONID", DUMMY_SESSION_ID)
                .header("X-XSRF-TOKEN", xsrfToken)
                .bodyValue(bodyJson)
                .exchange();

        // ## Assert ##
        responseSpec.expectStatus().isForbidden();
    }

    private void loginFailure_NoXXSRFTokenInHeader(String xsrfToken) {
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
                .cookie("JSESSIONID", DUMMY_SESSION_ID)
                /*
                .header("X-XSRF-TOKEN", xsrfToken)
                 */
                .bodyValue(bodyJson)
                .exchange();

        // ## Assert ##
        responseSpec.expectStatus().isForbidden();
    }

    private void loginFailure_DifferentToken(String xsrfToken) {
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
                .cookie("JSESSIONID", DUMMY_SESSION_ID)
                .header("X-XSRF-TOKEN", xsrfToken + "_invalid")
                .bodyValue(bodyJson)
                .exchange();

        // ## Assert ##
        responseSpec.expectStatus().isForbidden();
    }

    private void loginFailure_GivenUsernameDoesNotExistInDatabase(String xsrfToken) {
        // ## Arrange ##
        var bodyJson = String.format("""
                {
                  "username": "%s",
                  "password": "%s"
                }
                """, TEST_USERNAME + "_invalid", TEST_PASSWORD);

        // ## Act ##
        var responseSpec = webTestClient
                .post().uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .cookie("XSRF-TOKEN", xsrfToken)
                .cookie("JSESSIONID", DUMMY_SESSION_ID)
                .header("X-XSRF-TOKEN", xsrfToken)
                .bodyValue(bodyJson)
                .exchange();

        // ## Assert ##
        responseSpec.expectStatus().isUnauthorized();
    }
}
