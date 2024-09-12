package com.example.blog.web.controller.article;

import com.example.blog.service.article.ArticleService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ArticleRestControllerGetArticleTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ArticleService articleService;
    @Autowired
    private UserService userService;

    @Test
    void setup() {
        // ## Arrange ##

        // ## Act ##

        // ## Assert ##
        assertThat(mockMvc).isNotNull();
        assertThat(articleService).isNotNull();
        assertThat(userService).isNotNull();
    }

    @Test
    @DisplayName("GET /articles/{articleId}: 記事の詳細を取得できる")
    void getArticle_success() throws Exception {
        // ## Arrange ##
        var expectedUser1 = userService.register("test_username1", "test_password1");
        var expectedArticle1 = articleService.create(expectedUser1.getId(), "test_title1", "test_body1");

        // ## Act ##
        var actual = mockMvc.perform(
                get("/articles/{articleId}", expectedArticle1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // ## Assert ##
        actual
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(expectedArticle1.getId()))
                .andExpect(jsonPath("$.title").value(expectedArticle1.getTitle()))
                .andExpect(jsonPath("$.body").value(expectedArticle1.getBody()))
                .andExpect(jsonPath("$.createdAt").value(expectedArticle1.getCreatedAt().toString()))
                .andExpect(jsonPath("$.updatedAt").value(expectedArticle1.getUpdatedAt().toString()))
                .andExpect(jsonPath("$.author.id").value(expectedUser1.getId()))
                .andExpect(jsonPath("$.author.username").value(expectedUser1.getUsername()))
        ;
    }

    @Test
    @DisplayName("GET /articles/{articleId}: 指定された記事IDが存在しないとき 404 を返す")
    void getArticle_404() throws Exception {
        // ## Arrange ##
        var invalidArticleId = 0;

        // ## Act ##
        var actual = mockMvc.perform(
                get("/articles/{articleId}", invalidArticleId)
                        .contentType(MediaType.APPLICATION_JSON)
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

}