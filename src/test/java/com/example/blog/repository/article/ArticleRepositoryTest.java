package com.example.blog.repository.article;

import com.example.blog.config.MybatisDefaultDatasourceTest;
import com.example.blog.repository.user.UserRepository;
import com.example.blog.service.article.ArticleEntity;
import com.example.blog.service.user.UserEntity;
import com.example.blog.util.TestDateTimeUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisDefaultDatasourceTest
class ArticleRepositoryTest {

    @Autowired
    private ArticleRepository cut;
    @Autowired
    private UserRepository userRepository;

    @Test
    public void test() {
        assertThat(cut).isNotNull();
    }

    @Test
    @DisplayName("selectById: 指定されたIDの記事が存在するとき、ArticleEntityを返す")
    @Sql(statements = {"""
            DELETE FROM articles;
            DELETE FROM users;
            
            INSERT INTO users (id, username, password, enabled)
            VALUES (1, 'test_user1', 'test_password_1', true);
            
            INSERT INTO articles (id, user_id, title, body, created_at, updated_at)
            VALUES (999, 1, 'title_999', 'body_999', '2010-10-01 00:00:00', '2010-11-01 00:00:00');
            """
    })
    public void selectById_returnArticleEntity() {
        // ## Arrange ##

        // ## Act ##
        var actual = cut.selectById(999);

        // ## Assert ##
        assertThat(actual)
                .isPresent()
                .hasValueSatisfying(article -> {
                    assertThat(article.getId()).isEqualTo(999);
                    assertThat(article.getTitle()).isEqualTo("title_999");
                    assertThat(article.getBody()).isEqualTo("body_999");
                    assertThat(article.getCreatedAt()).isEqualTo("2010-10-01T00:00:00+09:00");
                    assertThat(article.getUpdatedAt()).isEqualTo("2010-11-01T00:00:00+09:00");

                    assertThat(article.getAuthor().getId()).isEqualTo(1);
                    assertThat(article.getAuthor().getUsername()).isEqualTo("test_user1");
                    assertThat(article.getAuthor().getPassword()).isNull();
                    assertThat(article.getAuthor().isEnabled()).isTrue();
                });
    }

    @Test
    @DisplayName("selectById: 指定されたIDの記事が存在しないとき、Optional.emptyを返す")
    public void selectById_returnEmpty() {
        // ## Arrange ##

        // ## Act ##
        var actual = cut.selectById(-999);

        // ## Assert ##
        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("insert：記事データの作成に成功する")
    void insert_success() {
        // ## Arrange ##
        var expectedUser = new UserEntity(null, "test_username", "test_password", true);
        userRepository.insert(expectedUser);

        var expectedEntity = new ArticleEntity(
                null,
                "test_title",
                "test_body",
                expectedUser,
                TestDateTimeUtil.of(2020, 1, 1, 10, 30, 40),
                TestDateTimeUtil.of(2021, 1, 1, 10, 30, 40)
        );

        // ## Act ##
        cut.insert(expectedEntity);

        // ## Assert ##
        var actualOpt = cut.selectById(expectedEntity.getId());
        assertThat(actualOpt).hasValueSatisfying(actualEntity -> {
            assertThat(actualEntity.getId()).isEqualTo(expectedEntity.getId());
            assertThat(actualEntity.getTitle()).isEqualTo(expectedEntity.getTitle());
            assertThat(actualEntity.getBody()).isEqualTo(expectedEntity.getBody());
            assertThat(actualEntity.getAuthor().getId()).isEqualTo(expectedUser.getId());
            assertThat(actualEntity.getAuthor().getUsername()).isEqualTo(expectedUser.getUsername());
            assertThat(actualEntity.getAuthor().getPassword()).isNull();
            assertThat(actualEntity.getAuthor().isEnabled()).isEqualTo(expectedUser.isEnabled());
            assertThat(actualEntity.getCreatedAt()).isEqualTo(expectedEntity.getCreatedAt());
            assertThat(actualEntity.getUpdatedAt()).isEqualTo(expectedEntity.getUpdatedAt());
        });

    }

    @Test
    @DisplayName("selectAll: 記事が存在しないとき、空のリストを返す")
    @Sql(statements = {"""
            DELETE FROM articles;
            """
    })
    void selectAll_returnEmptyList() {
        // ## Arrange ##

        // ## Act ##
        var actual = cut.selectAll();

        // ## Assert ##
        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("selectAll: 記事が存在するとき、ArticleEntity のリストを返す")
    @Sql(statements = {"""
            DELETE FROM articles;
            """
    })
    void selectAll_returnNonEmptyList() {
        // ## Arrange ##
        var datetime1 = TestDateTimeUtil.of(2020, 1, 10, 10, 10, 10);
        var datetime2 = TestDateTimeUtil.of(2021, 1, 10, 10, 10, 10);

        var user1 = new UserEntity();
        user1.setUsername("test_username1");
        user1.setPassword("test_password1");
        user1.setEnabled(true);
        userRepository.insert(user1);

        var expectedArticle1 = new ArticleEntity(null, "test_title1", "test_body1", user1, datetime1, datetime1);
        var expectedArticle2 = new ArticleEntity(null, "test_title2", "test_body2", user1, datetime2, datetime2);

        cut.insert(expectedArticle1);
        cut.insert(expectedArticle2);

        // ## Act ##
        var actual = cut.selectAll();

        // ## Assert ##
        assertThat(actual).hasSize(2);
        assertThat(actual.get(0))
                .usingRecursiveComparison()
                .ignoringFields("author.password")
                .isEqualTo(expectedArticle2);
        assertThat(actual.get(1))
                .usingRecursiveComparison()
                .ignoringFields("author.password")
                .isEqualTo(expectedArticle1);
    }

    @Test
    @DisplayName("update: 記事の title/body/updated_at を更新できる")
    void update_success() {
        // ## Arrange ##
        var expectedTitle = "test_title_updated";
        var expectedBody = "test_body_updated";
        var expectedCreatedAt = TestDateTimeUtil.of(2020, 1, 10, 10, 20, 30);
        var expectedUpdatedAt = expectedCreatedAt.plusDays(1);

        var expectedUser = new UserEntity(null, "test_username", "test_password", true);
        userRepository.insert(expectedUser);

        var articleToCreate = new ArticleEntity(
                null,
                "test_title",
                "test_body",
                expectedUser,
                expectedCreatedAt,
                expectedCreatedAt // 記事を新規作成したため createAt = updatedAt
        );
        cut.insert(articleToCreate);

        var articleToUpdate = new ArticleEntity(
                articleToCreate.getId(),
                expectedTitle,
                expectedBody,
                articleToCreate.getAuthor(),
                articleToCreate.getCreatedAt(),
                expectedUpdatedAt
        );

        // ## Act ##
        cut.update(articleToUpdate);

        // ## Assert ##
        var actual = cut.selectById(articleToCreate.getId());
        assertThat(actual).hasValueSatisfying(actualArticle -> {
            assertThat(actualArticle)
                    .usingRecursiveComparison()
                    .ignoringFields("author.password")
                    .isEqualTo(articleToUpdate);
        });
    }

    @Test
    @DisplayName("update: 指定された記事IDが存在しないとき、更新しない")
    void update_invalidArticleId() {
        // ## Arrange ##
        var expectedTitle = "test_title_updated";
        var expectedBody = "test_body_updated";
        var expectedCreatedAt = TestDateTimeUtil.of(2020, 1, 10, 10, 20, 30);
        var expectedUpdatedAt = expectedCreatedAt.plusDays(1);

        var expectedUser = new UserEntity(null, "test_username", "test_password", true);
        userRepository.insert(expectedUser);

        var articleToCreate = new ArticleEntity(
                null,
                "test_title",
                "test_body",
                expectedUser,
                expectedCreatedAt,
                expectedCreatedAt // 記事を新規作成したため createAt = updatedAt
        );
        cut.insert(articleToCreate);

        var articleToUpdate = new ArticleEntity(
                0L, // invalid article id
                expectedTitle,
                expectedBody,
                articleToCreate.getAuthor(),
                articleToCreate.getCreatedAt(),
                expectedUpdatedAt
        );

        // ## Act ##
        cut.update(articleToUpdate);

        // ## Assert ##
        var actual = cut.selectById(articleToCreate.getId());
        assertThat(actual).hasValueSatisfying(actualArticle -> {
            assertThat(actualArticle)
                    .usingRecursiveComparison()
                    .ignoringFields("author.password")
                    .isEqualTo(articleToCreate);
        });
    }

    @Test
    @DisplayName("update: 著者以外のユーザーIDが指定されているとき、更新しない")
    void update_invalidAuthorId() {
        // ## Arrange ##
        var expectedTitle = "test_title_updated";
        var expectedBody = "test_body_updated";
        var expectedCreatedAt = TestDateTimeUtil.of(2020, 1, 10, 10, 20, 30);
        var expectedUpdatedAt = expectedCreatedAt.plusDays(1);

        var author = new UserEntity(null, "test_username1", "test_password1", true);
        userRepository.insert(author);

        var otherUser = new UserEntity(null, "test_username2", "test_password2", true);
        userRepository.insert(otherUser);

        var articleToCreate = new ArticleEntity(
                null,
                "test_title",
                "test_body",
                author,
                expectedCreatedAt,
                expectedCreatedAt // 記事を新規作成したため createAt = updatedAt
        );
        cut.insert(articleToCreate);

        var articleToUpdate = new ArticleEntity(
                articleToCreate.getId(),
                expectedTitle,
                expectedBody,
                otherUser,
                articleToCreate.getCreatedAt(),
                expectedUpdatedAt
        );

        // ## Act ##
        cut.update(articleToUpdate);

        // ## Assert ##
        var actual = cut.selectById(articleToCreate.getId());
        assertThat(actual).hasValueSatisfying(actualArticle -> {
            assertThat(actualArticle)
                    .usingRecursiveComparison()
                    .ignoringFields("author.password")
                    .isEqualTo(articleToCreate);
        });
    }

    @Test
    @DisplayName("delete: 指定された ID の記事を削除する")
    void delete_success() {
        // ## Arrange ##
        var author = new UserEntity(null, "test_username", "test_password", true);
        userRepository.insert(author);

        var existingArticle = new ArticleEntity(
                null,
                "test_title",
                "test_body",
                author,
                TestDateTimeUtil.of(2020, 1, 10, 10, 20, 30),
                TestDateTimeUtil.of(2020, 1, 10, 10, 20, 30)
        );
        cut.insert(existingArticle);

        // ## Act ##
        cut.delete(existingArticle);

        // ## Assert ##
        var actual = cut.selectById(existingArticle.getId());
        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("delete: 指定された記事IDが存在しないとき、削除しない")
    void delete_invalidArticleId() {
        // ## Arrange ##
        var author = new UserEntity(null, "test_username", "test_password", true);
        userRepository.insert(author);

        var existingArticle = new ArticleEntity(
                null,
                "test_title",
                "test_body",
                author,
                TestDateTimeUtil.of(2020, 1, 10, 10, 20, 30),
                TestDateTimeUtil.of(2020, 1, 10, 10, 20, 30)
        );
        cut.insert(existingArticle);

        var articleToUpdate = new ArticleEntity(
                0L, // invalid article id
                existingArticle.getTitle(),
                existingArticle.getBody(),
                existingArticle.getAuthor(),
                existingArticle.getCreatedAt(),
                existingArticle.getUpdatedAt()
        );

        // ## Act ##
        cut.delete(articleToUpdate);

        // ## Assert ##
        var actual = cut.selectById(existingArticle.getId());
        assertThat(actual).hasValueSatisfying(actualArticle -> {
            assertThat(actualArticle)
                    .usingRecursiveComparison()
                    .ignoringFields("author.password")
                    .isEqualTo(existingArticle);
        });
    }

    @Test
    @DisplayName("delete: 著者以外のユーザーIDが指定されているとき、削除しない")
    void delete_invalidAuthorId() {
        // ## Arrange ##
        var author = new UserEntity(null, "test_username1", "test_password1", true);
        userRepository.insert(author);

        var otherUser = new UserEntity(null, "test_username2", "test_password2", true);
        userRepository.insert(otherUser);

        var existingArticle = new ArticleEntity(
                null,
                "test_title",
                "test_body",
                author,
                TestDateTimeUtil.of(2020, 1, 10, 10, 20, 30),
                TestDateTimeUtil.of(2020, 1, 10, 10, 20, 30)
        );
        cut.insert(existingArticle);

        var articleToDelete = new ArticleEntity(
                existingArticle.getId(),
                existingArticle.getTitle(),
                existingArticle.getBody(),
                otherUser,
                existingArticle.getCreatedAt(),
                existingArticle.getUpdatedAt()
        );

        // ## Act ##
        cut.delete(articleToDelete);

        // ## Assert ##
        var actual = cut.selectById(existingArticle.getId());
        assertThat(actual).hasValueSatisfying(actualArticle -> {
            assertThat(actualArticle)
                    .usingRecursiveComparison()
                    .ignoringFields("author.password")
                    .isEqualTo(existingArticle);
        });
    }
}