package com.example.blog.service.article;

import com.example.blog.repository.article.ArticleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
class ArticleServiceMockTest {

    @Mock
    private ArticleRepository articleRepository;

    @InjectMocks
    private ArticleService cut;

    @Test
    public void cut() {
        assertThat(cut).isNotNull();
    }

}