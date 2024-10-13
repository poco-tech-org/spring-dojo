package com.example.blog.web.controller.article;

import com.example.blog.security.LoggedInUser;
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
    void createArticleComment_201Created() throws Exception {
        // ## Arrange ##
        var articleAuthor = userService.register("test_username1", "test_password1");
        var article = articleService.create(
                articleAuthor.getId(),
                "test_article_title",
                "test_article_body"
        );
        var commentAuthor = userService.register("test_username2", "test_password2");
        var loggedInCommentAuthor = new LoggedInUser(
                commentAuthor.getId(),
                commentAuthor.getUsername(),
                commentAuthor.getPassword(),
                commentAuthor.isEnabled()
        );
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
}