package com.example.blog.repository.article;

import com.example.blog.config.MybatisDefaultDatasourceTest;
import com.example.blog.repository.user.UserRepository;
import com.example.blog.service.article.ArticleCommentEntity;
import com.example.blog.service.article.ArticleEntity;
import com.example.blog.service.user.UserEntity;
import com.example.blog.util.TestDateTimeUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@MybatisDefaultDatasourceTest
class ArticleCommentRepositoryTest {

    @Autowired
    private ArticleCommentRepository cut;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ArticleRepository articleRepository;

    @Test
    @DisplayName("insert：記事データの作成に成功する")
    void insert_success() {
        // ## Arrange ##
        var articleAuthor = new UserEntity(
                null,
                "test_username1",
                "test_password1",
                true
        );
        userRepository.insert(articleAuthor);

        var article = new ArticleEntity(
                null,
                "test_title",
                "test_body",
                articleAuthor,
                TestDateTimeUtil.of(2020, 1, 1, 10, 30, 40),
                TestDateTimeUtil.of(2021, 1, 1, 10, 30, 40)
        );
        articleRepository.insert(article);

        var commentAuthor = new UserEntity(
                null,
                "test_username2",
                "test_password2",
                true
        );
        userRepository.insert(commentAuthor);

        var comment = new ArticleCommentEntity(
                null,
                "test_comment_body",
                article,
                commentAuthor,
                TestDateTimeUtil.of(2022, 1, 1, 10, 30, 40)
        );

        // ## Act ##
        cut.insert(comment);

        // ## Assert ##
        var actualOpt = cut.selectById(comment.getId());
        assertThat(actualOpt).hasValueSatisfying(actualEntity -> {
            assertThat(actualEntity)
                    .usingRecursiveComparison()
                    .ignoringFields(
                            "author.password",
                            "article.author.password"
                    )
                    .isEqualTo(comment);
        });

    }
}