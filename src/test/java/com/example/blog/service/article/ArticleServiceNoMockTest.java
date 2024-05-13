package com.example.blog.service.article;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ArticleServiceNoMockTest {

    @Autowired
    private ArticleService cut;

    @Test
    public void cut() {
        assertThat(cut).isNotNull();
    }

}