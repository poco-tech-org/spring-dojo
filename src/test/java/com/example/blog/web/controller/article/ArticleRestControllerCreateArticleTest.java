package com.example.blog.web.controller.article;

import com.example.blog.security.LoggedInUser;
import com.example.blog.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.hasEntry;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ArticleRestControllerCreateArticleTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserService userService;

    @Test
    void setup() {
        // ## Arrange ##

        // ## Act ##

        // ## Assert ##
        assertThat(mockMvc).isNotNull();
        assertThat(userService).isNotNull();
    }

    @Test
    @DisplayName("POST /articles: 新規記事の作成に成功する")
    void createArticles_201Created() throws Exception {
        // ## Arrange ##
        var newUser = userService.register("test_username", "test_password");
        var expectedUser = new LoggedInUser(newUser.getId(), newUser.getUsername(), newUser.getPassword(), true);
        var expectedTitle = "test_title";
        var expectedBody = "test_body";
        var bodyJson = """
                {
                  "title": "%s",
                  "body": "%s"
                }
                """.formatted(expectedTitle, expectedBody);

        // ## Act ##
        var actual = mockMvc.perform(
                post("/articles")
                        .with(csrf())
                        .with(user(expectedUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson)
        );

        // ## Assert ##
        actual
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Location", matchesPattern("/articles/\\d+")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value(expectedTitle))
                .andExpect(jsonPath("$.body").value(expectedBody))
                .andExpect(jsonPath("$.author.id").value(expectedUser.getUserId()))
                .andExpect(jsonPath("$.author.username").value(expectedUser.getUsername()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty())
        ;
    }

    @Test
    @DisplayName("POST /articles: リクエストの title フィールドがバリデーションNGのとき、400 BadRequest")
    void createArticles_400BadRequest() throws Exception {
        // ## Arrange ##
        var newUser = userService.register("test_username", "test_password");
        var expectedUser = new LoggedInUser(newUser.getId(), newUser.getUsername(), newUser.getPassword(), true);
        var bodyJson = """
                {
                  "title": "",
                  "body": "OK_body"
                }
                """;

        // ## Act ##
        var actual = mockMvc.perform(
                post("/articles")
                        .with(csrf())
                        .with(user(expectedUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson)
        );

        // ## Assert ##
        actual
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request content."))
                .andExpect(jsonPath("$.instance").value("/articles"))
                .andExpect(jsonPath("$.errors", hasItem(
                        allOf(
                                hasEntry("pointer", "#/title"),
                                hasEntry("detail", "タイトルは1文字以上255文字以内で入力してください。")
                        )
                )))
        ;
    }

    @Test
    @DisplayName("POST /articles: 未ログインのとき、401 Unauthorized を返す")
    void createArticles_401Unauthorized() throws Exception {
        // ## Arrange ##

        // ## Act ##
        var actual = mockMvc.perform(
                post("/articles")
                        .with(csrf())
                // .with(user("user1")) // 未ログイン状態
        );

        // ## Assert ##
        actual
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").value("リクエストを実行するにはログインが必要です"))
                .andExpect(jsonPath("$.instance").value("/articles"))
        ;
    }

    @Test
    @DisplayName("POST /articles: リクエストに CSRF トークンが付加されていないとき 403 Forbidden を返す")
    void createArticles_403Forbidden() throws Exception {
        // ## Arrange ##

        // ## Act ##
        var actual = mockMvc.perform(
                post("/articles")
                        // .with(csrf()) // CSRF トークンを付加しない
                        .with(user("user1"))
        );

        // ## Assert ##
        actual
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Forbidden"))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.detail").value("CSRFトークンが不正です"))
                .andExpect(jsonPath("$.instance").value("/articles"))
        ;
    }
}