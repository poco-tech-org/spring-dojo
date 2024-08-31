package com.example.blog.service.article;

import com.example.blog.repository.article.ArticleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


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

    @Test
    public void mockPractice() {
        when(articleRepository.selectById(999)).thenReturn(Optional.of(
                new ArticleEntity(999L, null, null, null, null)
        ));

        assertThat(articleRepository.selectById(999))
                .isPresent()
                .hasValueSatisfying(article -> {
                    assertThat(article.id()).isEqualTo(999);
                });
        assertThat(articleRepository.selectById(111)).isEmpty();
    }

    @Test
    @DisplayName("findById: 指定されたIDの記事が存在するとき、ArticleEntity を返す")
    public void findById_returnArticleEntity() {
        // ## Arrange ##
        when(articleRepository.selectById(999)).thenReturn(Optional.of(
                new ArticleEntity(
                        999L,
                        "title_999",
                        "body_999",
                        LocalDateTime.of(2022, 1, 1, 10, 0,0, 0),
                        LocalDateTime.of(2022, 2, 1, 11, 0,0, 0))
        ));

        // ## Act ##
        var actual = cut.findById(999);

        // ## Assert ##
        assertThat(actual)
                .isPresent()
                .hasValueSatisfying(article -> {
                    assertThat(article.id()).isEqualTo(999);
                    assertThat(article.title()).isEqualTo("title_999");
                    assertThat(article.content()).isEqualTo("body_999");
                    assertThat(article.createdAt()).isEqualTo("2022-01-01T10:00:00");
                    assertThat(article.updatedAt()).isEqualTo("2022-02-01T11:00:00");
                });

    }

    @Test
    @DisplayName("findById: 指定されたIDの記事が存在しないとき、Optional.Empty を返す")
    public void findById_returnEmpty() {
        // ## Arrange ##
        when(articleRepository.selectById(999)).thenReturn(Optional.empty());

        // ## Act ##
        var actual = cut.findById(999);

        // ## Assert ##
        assertThat(actual).isEmpty();
    }
}