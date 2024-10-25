package com.example.blog.web.controller.article;


import com.example.blog.security.LoggedInUser;
import com.example.blog.service.article.ArticleCommentService;
import com.example.blog.service.article.ArticleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ArticleRestController500InternalServerErrorTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ArticleService articleService;
    @MockBean
    private ArticleCommentService articleCommentService;

    @Test
    void setUp_success() {
        // ## Arrange ##

        // ## Act ##

        // ## Assert ##
        assertThat(mockMvc).isNotNull();
        assertThat(articleService).isNotNull();
    }

    @Test
    @DisplayName("POST /articles: 500 InternalServerError で stacktrace が露出しない")
    void createArticle_500() throws Exception {
        // ## Arrange ##
        var userId = 999L;
        var title = "test_title";
        var body = "test_body";
        when(articleService.create(userId, title, body)).thenThrow(RuntimeException.class);

        var bodyJson = """
                {
                  "title": "%s",
                  "body": "%s"
                }
                """.formatted(title, body);

        // ## Act ##
        var actual = mockMvc.perform(
                post("/articles")
                        .with(csrf())
                        .with(user(new LoggedInUser(userId, "test_username", "", true)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson)
        );

        // ## Assert ##
        actual
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").isEmpty())
                .andExpect(jsonPath("$.instance").value("/articles"))
                .andExpect(jsonPath("$", aMapWithSize(4)))
        ;
    }

    @Test
    @DisplayName("GET /articles: 500 InternalServerError で stacktrace が露出しない")
    void listArticles_500() throws Exception {
        // ## Arrange ##
        when(articleService.findAll()).thenThrow(RuntimeException.class);

        // ## Act ##
        var actual = mockMvc.perform(
                get("/articles")
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // ## Assert ##
        actual
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").isEmpty())
                .andExpect(jsonPath("$.instance").value("/articles"))
                .andExpect(jsonPath("$", aMapWithSize(4)))
        ;
    }

    @Test
    @DisplayName("GET /articles/{articleId}: 500 InternalServerError で stacktrace が露出しない")
    void getArticle_500() throws Exception {
        // ## Arrange ##
        var articleId = 999;
        when(articleService.findById(articleId)).thenThrow(RuntimeException.class);

        // ## Act ##
        var actual = mockMvc.perform(
                get("/articles/{articleId}", articleId)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // ## Assert ##
        actual
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").isEmpty())
                .andExpect(jsonPath("$.instance").value("/articles/" + articleId))
                .andExpect(jsonPath("$", aMapWithSize(4)))
        ;
    }

    @Test
    @DisplayName("PUT /articles/{articleId}: 500 InternalServerError で stacktrace が露出しない")
    void putArticle_500() throws Exception {
        // ## Arrange ##
        var userId = 999L;
        var articleId = 9999L;
        var title = "test_title";
        var body = "test_body";
        when(articleService.update(userId, articleId, title, body)).thenThrow(RuntimeException.class);

        var bodyJson = """
                {
                  "title": "%s",
                  "body": "%s"
                }
                """.formatted(title, body);

        // ## Act ##
        var actual = mockMvc.perform(
                put("/articles/{articleId}", articleId)
                        .with(csrf())
                        .with(user(new LoggedInUser(userId, "test_username", "", true)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson)
        );

        // ## Assert ##
        actual
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").isEmpty())
                .andExpect(jsonPath("$.instance").value("/articles/" + articleId))
                .andExpect(jsonPath("$", aMapWithSize(4)))
        ;
    }

    @Test
    @DisplayName("DELETE /articles/{articleId}: 500 InternalServerError で stacktrace が露出しない")
    void deleteArticle_500() throws Exception {
        // ## Arrange ##
        var userId = 999L;
        var articleId = 9999L;
        doThrow(RuntimeException.class).when(articleService).delete(userId, articleId);

        // ## Act ##
        var actual = mockMvc.perform(
                delete("/articles/{articleId}", articleId)
                        .with(csrf())
                        .with(user(new LoggedInUser(userId, "test_username", "", true)))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // ## Assert ##
        actual
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").isEmpty())
                .andExpect(jsonPath("$.instance").value("/articles/" + articleId))
                .andExpect(jsonPath("$", aMapWithSize(4)))
        ;
    }

    @Test
    @DisplayName("POST /articles/{articleId}/comments: 500 InternalServerError で stacktrace が露出しない")
    void createArticleComments_500() throws Exception {
        // ## Arrange ##
        var userId = 999L;
        var articleId = 9999L;
        var body = "test_body";
        doThrow(RuntimeException.class).when(articleCommentService).create(userId, articleId, body);

        var bodyJson = """
                {
                  "body": "%s"
                }
                """.formatted(body);

        // ## Act ##
        var actual = mockMvc.perform(
                post("/articles/{articleId}/comments", articleId)
                        .with(csrf())
                        .with(user(new LoggedInUser(userId, "test_username", "", true)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson)
        );

        // ## Assert ##
        actual
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").isEmpty())
                .andExpect(jsonPath("$.instance").value("/articles/%d/comments".formatted(articleId)))
                .andExpect(jsonPath("$", aMapWithSize(4)))
        ;
    }

    @Test
    @DisplayName("GET /articles/{articleId}/comments: 500 InternalServerError で stacktrace が露出しない")
    void listArticleComments_500() throws Exception {
        // ## Arrange ##
        var articleId = 9999L;
        doThrow(RuntimeException.class).when(articleCommentService).findByArticleId(articleId);

        // ## Act ##
        var actual = mockMvc.perform(
                get("/articles/{articleId}/comments", articleId)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // ## Assert ##
        actual
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").isEmpty())
                .andExpect(jsonPath("$.instance").value("/articles/%d/comments".formatted(articleId)))
                .andExpect(jsonPath("$", aMapWithSize(4)))
        ;
    }

}
