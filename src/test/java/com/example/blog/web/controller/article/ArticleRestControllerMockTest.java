package com.example.blog.web.controller.article;

import com.example.blog.service.article.ArticleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(ArticleRestController.class)
class ArticleRestControllerMockTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArticleService mockArticleService;

    @Test
    public void mockMvc() {
        assertThat(mockMvc).isNotNull();
    }

}