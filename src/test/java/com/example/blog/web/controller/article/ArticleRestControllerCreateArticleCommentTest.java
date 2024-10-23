package com.example.blog.web.controller.article;

import com.example.blog.security.LoggedInUser;
import com.example.blog.service.article.ArticleEntity;
import com.example.blog.service.article.ArticleService;
import com.example.blog.service.user.UserEntity;
import com.example.blog.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ArticleRestControllerCreateArticleCommentTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserService userService;
    @Autowired
    private ArticleService articleService;

    private ArticleEntity article;
    private UserEntity commentAuthor;
    private LoggedInUser loggedInCommentAuthor;

    @BeforeEach
    void beforeEach() {
        var articleAuthor = userService.register("test_username1", "test_password1");
        article = articleService.create(
                articleAuthor.getId(),
                "test_article_title",
                "test_article_body"
        );
        commentAuthor = userService.register("test_username2", "test_password2");
        loggedInCommentAuthor = new LoggedInUser(
                commentAuthor.getId(),
                commentAuthor.getUsername(),
                commentAuthor.getPassword(),
                commentAuthor.isEnabled()
        );
    }

    @Test
    void setup() {
        // ## Arrange ##

        // ## Act ##

        // ## Assert ##
        assertThat(mockMvc).isNotNull();
        assertThat(userService).isNotNull();
    }

    @Test
    @DisplayName("POST /articles/{articleId}/comments: 新規コメントの作成に成功する")
    void createArticleComments_201Created() throws Exception {
        // ## Arrange ##
        var expectedBody = "記事にコメントをしました";
        var bodyJson = """
                {
                  "body": "%s"
                }
                """.formatted(expectedBody);

        // ## Act ##
        var actual = mockMvc.perform(
                post("/articles/{articleId}/comments", article.getId())
                        .with(csrf())
                        .with(user(loggedInCommentAuthor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson)
        );

        // ## Assert ##
        actual
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Location", matchesPattern(
                        "/articles/" + article.getId() + "/comments/\\d+"
                )))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.body").value(expectedBody))
                .andExpect(jsonPath("$.author.id").value(commentAuthor.getId()))
                .andExpect(jsonPath("$.author.username").value(commentAuthor.getUsername()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
        ;
    }

    @Test
    @DisplayName("POST /articles/{articleId}/comments: リクエストの body フィールドが空のとき、400 BadRequest")
    void createArticleComments_400BadRequest() throws Exception {
        // ## Arrange ##
        var bodyJson = """
                {
                  "body": ""
                }
                """;

        // ## Act ##
        var actual = mockMvc.perform(
                post("/articles/{articleId}/comments", article.getId())
                        .with(csrf())
                        .with(user(loggedInCommentAuthor))
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
                .andExpect(jsonPath("$.instance").value("/articles/%d/comments".formatted(article.getId())))
                .andExpect(jsonPath("$.errors", hasItem(
                        allOf(
                                hasEntry("pointer", "#/body"),
                                hasEntry("detail", "コメント本文は必須です")
                        )
                )))
        ;
    }

    @Test
    @DisplayName("POST /articles/{articleId}/comments: 未ログインのとき、401 Unauthorized を返す")
    void createArticleComments_401Unauthorized() throws Exception {
        // ## Arrange ##
        var bodyJson = """
                {
                  "body": "test_body"
                }
                """;

        // ## Act ##
        var actual = mockMvc.perform(
                post("/articles/{articleId}/comments", article.getId())
                        .with(csrf())
                        // .with(user(loggedInCommentAuthor)) // 未ログイン状態
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson)
        );

        // ## Assert ##
        actual
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").value("リクエストを実行するにはログインが必要です"))
                .andExpect(jsonPath("$.instance").value("/articles/%d/comments".formatted(article.getId())))
        ;
    }

    @Test
    @DisplayName("POST /articles/{articleId}/comments: リクエストに CSRF トークンが付加されていないとき 403 Forbidden を返す")
    void createArticleComments_403Forbidden() throws Exception {
        // ## Arrange ##
        var bodyJson = """
                {
                  "body": "test_body"
                }
                """;

        // ## Act ##
        var actual = mockMvc.perform(
                post("/articles/{articleId}/comments", article.getId())
                        // .with(csrf()) // CSRF トークンがない
                        .with(user(loggedInCommentAuthor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson)
        );

        // ## Assert ##
        actual
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Forbidden"))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.detail").value("CSRFトークンが不正です"))
                .andExpect(jsonPath("$.instance").value("/articles/%d/comments".formatted(article.getId())))
        ;
    }

    @Test
    @DisplayName("POST /articles/{articleId}/comments: 指定されたIDの記事が存在しないとき、404を返す")
    void deleteArticle_404NotFound() throws Exception {
        // ## Arrange ##
        var invalidArticleId = 0;
        var bodyJson = """
                {
                  "body": "test_body"
                }
                """;

        // ## Act ##
        var actual = mockMvc.perform(
                post("/articles/{articleId}/comments", invalidArticleId)
                        .with(csrf())
                        .with(user(loggedInCommentAuthor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson)
        );

        // ## Assert ##
        actual
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("リソースが見つかりません"))
                .andExpect(jsonPath("$.instance").value("/articles/%d/comments".formatted(invalidArticleId)))
        ;
    }

}