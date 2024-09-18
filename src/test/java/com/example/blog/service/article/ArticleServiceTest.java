package com.example.blog.service.article;

import com.example.blog.config.MybatisDefaultDatasourceTest;
import com.example.blog.repository.article.ArticleRepository;
import com.example.blog.repository.user.UserRepository;
import com.example.blog.service.DateTimeService;
import com.example.blog.service.exception.ResourceNotFoundException;
import com.example.blog.service.exception.UnauthorizedResourceAccessException;
import com.example.blog.service.user.UserEntity;
import com.example.blog.util.TestDateTimeUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@MybatisDefaultDatasourceTest
@Import(ArticleService.class)
class ArticleServiceTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ArticleService cut;
    @MockBean
    private DateTimeService mockDateTimeService;
    @Autowired
    private ArticleRepository articleRepository;

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

        var expectedCurrentDateTime = TestDateTimeUtil.of(2020, 1, 2, 10, 20, 30);
        when(mockDateTimeService.now()).thenReturn(expectedCurrentDateTime);

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
        assertThat(actual.getCreatedAt()).isEqualTo(expectedCurrentDateTime);
        assertThat(actual.getUpdatedAt()).isEqualTo(expectedCurrentDateTime);
    }

    @Test
    @DisplayName("findAll: 記事が1件も存在しないとき、空のリストを返す")
    @Sql(statements = {"""
            DELETE FROM articles;
            """})
    void findAll_returnEmptyList() {
        // ## Arrange ##


        // ## Act ##
        var actual = cut.findAll();

        // ## Assert ##
        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("findAll: 記事が存在するとき、リストを返す")
    @Sql(statements = {"""
            DELETE FROM articles;
            """})
    void findAll_returnNonEmptyList() {
        // ## Arrange ##
        when(mockDateTimeService.now())
                .thenReturn(TestDateTimeUtil.of(2020, 1, 10, 10, 10, 10))
                .thenReturn(TestDateTimeUtil.of(2021, 1, 10, 10, 10, 10));

        var user1 = new UserEntity();
        user1.setUsername("test_username1");
        user1.setPassword("test_password1");
        user1.setEnabled(true);
        userRepository.insert(user1);

        var expectedArticle1 = cut.create(user1.getId(), "test_title1", "test_body1");
        var expectedArticle2 = cut.create(user1.getId(), "test_title2", "test_body2");

        // ## Act ##
        var actual = cut.findAll();

        // ## Assert ##
        assertThat(actual).hasSize(2);
        assertThat(actual.get(0)).isEqualTo(expectedArticle2);
        assertThat(actual.get(1)).isEqualTo(expectedArticle1);
    }

    @Test
    @DisplayName("update: 記事の更新に成功する")
    void update_success() {
        // ## Arrange ##
        var expectedUpdatedAt = TestDateTimeUtil.of(2020, 1, 10, 10, 10, 10);
        when(mockDateTimeService.now())
                .thenReturn(expectedUpdatedAt.minusDays(1))
                .thenReturn(expectedUpdatedAt);

        var expectedUser = new UserEntity();
        expectedUser.setUsername("test_user1");
        expectedUser.setPassword("test_password1");
        expectedUser.setEnabled(true);
        userRepository.insert(expectedUser);

        var existingArticle = cut.create(expectedUser.getId(), "test_title", "test_body");

        var expectedTitle = "test_title_updated";
        var expectedBody = "test_body_updated";


        // ## Act ##
        var actual = cut.update(expectedUser.getId(), existingArticle.getId(), expectedTitle, expectedBody);

        // ## Assert ##

        // assert return value of ArticleService#update
        assertThat(actual.getId()).isEqualTo(existingArticle.getId());
        assertThat(actual.getTitle()).isEqualTo(expectedTitle);
        assertThat(actual.getBody()).isEqualTo(expectedBody);
        assertThat(actual.getCreatedAt()).isEqualTo(existingArticle.getCreatedAt());
        assertThat(actual.getUpdatedAt()).isEqualTo(expectedUpdatedAt);
        assertThat(actual.getAuthor().getId()).isEqualTo(expectedUser.getId());
        assertThat(actual.getAuthor().getUsername()).isEqualTo(expectedUser.getUsername());
        assertThat(actual.getAuthor().getPassword()).isNull();
        assertThat(actual.getAuthor().isEnabled()).isEqualTo(expectedUser.isEnabled());

        // assert record in database
        var actualRecordOpt = articleRepository.selectById(existingArticle.getId());
        assertThat(actualRecordOpt).hasValueSatisfying(actualRecord -> {
            assertThat(actualRecord.getId()).isEqualTo(existingArticle.getId());
            assertThat(actualRecord.getTitle()).isEqualTo(expectedTitle);
            assertThat(actualRecord.getBody()).isEqualTo(expectedBody);
            assertThat(actualRecord.getCreatedAt()).isEqualTo(existingArticle.getCreatedAt());
            assertThat(actualRecord.getUpdatedAt()).isEqualTo(expectedUpdatedAt);
            assertThat(actualRecord.getAuthor().getId()).isEqualTo(expectedUser.getId());
            assertThat(actualRecord.getAuthor().getUsername()).isEqualTo(expectedUser.getUsername());
            assertThat(actualRecord.getAuthor().getPassword()).isNull();
            assertThat(actualRecord.getAuthor().isEnabled()).isEqualTo(expectedUser.isEnabled());
        });
    }

    @Test
    @DisplayName("update: 指定された ID の記事が見つからないとき、ResourceNotFoundException を throw する")
    void update_throwResourceNotFoundException() {
        // ## Arrange ##
        var expectedUser = new UserEntity();
        expectedUser.setUsername("test_user1");
        expectedUser.setPassword("test_password1");
        expectedUser.setEnabled(true);
        userRepository.insert(expectedUser);

        var invalidArticleId = 0;

        // ## Act & Assert ##
        assertThrows(ResourceNotFoundException.class, () -> {
            cut.update(expectedUser.getId(), invalidArticleId, "test_title_updated", "test_body_updated");
        });
    }

    @Test
    @DisplayName("update: 他人の記事を編集しようとしたとき UnauthorizedResourceAccessException を throw する")
    void update_throwUnauthorizedResourceAccessException() {
        // ## Arrange ##
        when(mockDateTimeService.now())
                .thenReturn(TestDateTimeUtil.of(2020, 1, 10, 10, 10, 10));

        var author = new UserEntity();
        author.setUsername("test_user1");
        author.setPassword("test_password1");
        author.setEnabled(true);
        userRepository.insert(author);

        var existingArticle = cut.create(author.getId(), "test_title", "test_body");

        var otherUser = new UserEntity();
        otherUser.setUsername("test_user2");
        otherUser.setPassword("test_password2");
        otherUser.setEnabled(true);
        userRepository.insert(otherUser);

        // ## Act & Assert ##
        assertThrows(UnauthorizedResourceAccessException.class, () -> {
            cut.update(otherUser.getId(), existingArticle.getId(), "test_title_updated", "test_body_updated");
        });
    }
}