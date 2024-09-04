package com.example.blog.service.article;

import com.example.blog.config.MybatisDefaultDatasourceTest;
import com.example.blog.repository.user.UserRepository;
import com.example.blog.service.user.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisDefaultDatasourceTest
@Import(ArticleService.class)
class ArticleServiceTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ArticleService cut;

    @Test
    void setup() {
        assertThat(userRepository).isNotNull();
        assertThat(cut).isNotNull();
    }

    @Test
    @DisplayName("create: articles テーブルにレコードが insert される")
    void create_success() {
        // ## Arrange ##
        var expectedUser = new UserEntity();
        expectedUser.setUsername("test_user1");
        expectedUser.setPassword("test_password1");
        expectedUser.setEnabled(true);
        userRepository.insert(expectedUser);

        var expectedTitle = "test_article_title";
        var expectedBody = "test_article_body";

        // ## Act ##
        var actual = cut.create(expectedUser.getId(), expectedTitle, expectedBody);

        // ## Assert ##
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getTitle()).isEqualTo(expectedTitle);
        assertThat(actual.getBody()).isEqualTo(expectedBody);
        assertThat(actual.getAuthor().getId()).isEqualTo(expectedUser.getId());
        assertThat(actual.getAuthor().getUsername()).isEqualTo(expectedUser.getUsername());
        assertThat(actual.getAuthor().getPassword()).isNull();
        assertThat(actual.getAuthor().isEnabled()).isEqualTo(expectedUser.isEnabled());
        assertThat(actual.getCreatedAt()).isNotNull();
        assertThat(actual.getUpdatedAt()).isEqualTo(actual.getCreatedAt());
    }
}