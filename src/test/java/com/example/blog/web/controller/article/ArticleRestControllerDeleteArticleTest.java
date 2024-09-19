package com.example.blog.web.controller.article;

import com.example.blog.security.LoggedInUser;
import com.example.blog.service.DateTimeService;
import com.example.blog.service.article.ArticleEntity;
import com.example.blog.service.article.ArticleService;
import com.example.blog.service.user.UserEntity;
import com.example.blog.service.user.UserService;
import com.example.blog.util.TestDateTimeUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ArticleRestControllerDeleteArticleTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserService userService;
    @Autowired
    private ArticleService articleService;
    @MockBean
    private DateTimeService mockDateTimeService;

    private ArticleEntity existingArticle;
    private UserEntity author;
    private LoggedInUser loggedInAuthor;

    @BeforeEach
    void beforeEach() {
        when(mockDateTimeService.now())
                .thenReturn(TestDateTimeUtil.of(2020, 1, 1, 10, 20, 30))
                .thenReturn(TestDateTimeUtil.of(2020, 2, 1, 10, 20, 30));

        author = userService.register("test_username", "test_password");
        existingArticle = articleService.create(author.getId(), "test_title", "test_body");
        loggedInAuthor = new LoggedInUser(author.getId(), author.getUsername(), author.getPassword(), true);
    }

    @Test
    void setup() {
        // ## Arrange ##

        // ## Act ##

        // ## Assert ##
        assertThat(mockMvc).isNotNull();
        assertThat(userService).isNotNull();
        assertThat(articleService).isNotNull();
    }

    @Test
    @DisplayName("DELETE /articles/{articleId}: 記事の削除に成功する")
    void deleteArticle_204NoContent() throws Exception {
        // ## Arrange ##

        // ## Act ##
        var actual = mockMvc.perform(
                delete("/articles/{articleId}", existingArticle.getId())
                        .with(csrf())
                        .with(user(loggedInAuthor))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // ## Assert ##
        actual
                .andExpect(status().isNoContent())
                .andExpect(content().string(is(emptyString())));
    }

    @Test
    @DisplayName("DELETE /articles/{articleId}: 未ログインのとき、401 Unauthorized を返す")
    void deleteArticle_401Unauthorized() throws Exception {
        // ## Arrange ##

        // ## Act ##
        var actual = mockMvc.perform(
                delete("/articles/{articleId}", existingArticle.getId())
                        .with(csrf())
                        //.with(user(loggedInAuthor))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // ## Assert ##
        actual
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").value("リクエストを実行するにはログインが必要です"))
                .andExpect(jsonPath("$.instance").value("/articles/" + existingArticle.getId()))
        ;
    }

    @Test
    @DisplayName("DELETE /articles/{articleId}: 自分が作成した記事以外の記事を削除しようとしたとき 403 を返す")
    void deleteArticle_403Forbidden_authorId() throws Exception {
        // ## Arrange ##
        var otherUser = userService.register("test_username2", "test_password2");
        var loggedInOtherUser = new LoggedInUser(otherUser.getId(), otherUser.getUsername(), otherUser.getPassword(), true);

        // ## Act ##
        var actual = mockMvc.perform(
                delete("/articles/{articleId}", existingArticle.getId())
                        .with(csrf())
                        .with(user(loggedInOtherUser))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // ## Assert ##
        actual
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Forbidden"))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.detail").value("リソースへのアクセスが拒否されました"))
                .andExpect(jsonPath("$.instance").value("/articles/" + existingArticle.getId()))
        ;
    }

    @Test
    @DisplayName("DELETE /articles/{articleId}: リクエストに CSRF トークンが付加されていないとき 403 Forbidden を返す")
    void deleteArticle_403Forbidden_csrf() throws Exception {
        // ## Arrange ##

        // ## Act ##
        var actual = mockMvc.perform(
                delete("/articles/{articleId}", existingArticle.getId())
                        // .with(csrf())
                        .with(user(loggedInAuthor))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // ## Assert ##
        actual
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Forbidden"))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.detail").value("CSRFトークンが不正です"))
                .andExpect(jsonPath("$.instance").value("/articles/" + existingArticle.getId()))
        ;
    }

    @Test
    @DisplayName("PUT /articles/{articleId}: 指定されたIDの記事が存在しないとき、404を返す")
    void updateArticle_404NotFound() throws Exception {
        // ## Arrange ##
        var invalidArticleId = 0;
        var bodyJson = """
                {
                  "title": "test_title_updated",
                  "body": "test_body_updated"
                }
                """;

        // ## Act ##
        var actual = mockMvc.perform(
                put("/articles/{articleId}", invalidArticleId)
                        .with(csrf())
                        .with(user(loggedInAuthor))
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
                .andExpect(jsonPath("$.instance").value("/articles/" + invalidArticleId))
        ;
    }

    @Test
    @DisplayName("PUT /articles/{articleId}: リクエストの title フィールドがバリデーションNGのとき、400 BadRequest")
    void updateArticle_400BadRequest() throws Exception {
        // ## Arrange ##
        var bodyJson = """
                {
                  "title": "",
                  "body": "test_body_updated"
                }
                """;

        // ## Act ##
        var actual = mockMvc.perform(
                put("/articles/{articleId}", existingArticle.getId())
                        .with(csrf())
                        .with(user(loggedInAuthor))
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
                .andExpect(jsonPath("$.instance").value("/articles/" + existingArticle.getId()))
                .andExpect(jsonPath("$.errors", hasItem(
                        allOf(
                                hasEntry("pointer", "#/title"),
                                hasEntry("detail", "タイトルは1文字以上255文字以内で入力してください。")
                        )
                )))
        ;
    }

}