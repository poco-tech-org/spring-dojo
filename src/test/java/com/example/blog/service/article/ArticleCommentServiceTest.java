package com.example.blog.service.article;

import com.example.blog.config.MybatisDefaultDatasourceTest;
import com.example.blog.config.PasswordEncoderConfig;
import com.example.blog.service.DateTimeService;
import com.example.blog.service.exception.ResourceNotFoundException;
import com.example.blog.service.user.UserService;
import com.example.blog.util.TestDateTimeUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@MybatisDefaultDatasourceTest
@Import({
        ArticleCommentService.class,
        ArticleService.class,
        UserService.class,
        PasswordEncoderConfig.class
})
class ArticleCommentServiceTest {

    @MockBean
    private DateTimeService mockDateTimeService;
    @Autowired
    private UserService userService;
    @Autowired
    private ArticleService articleService;
    @Autowired
    private ArticleCommentService cut;

    @Test
    @DisplayName("create: articles テーブルにレコードが insert される")
    void create_success() {
        // ## Arrange ##
        var expectedCurrentDateTime = TestDateTimeUtil.of(2020, 1, 2, 10, 20, 30);
        when(mockDateTimeService.now()).thenReturn(expectedCurrentDateTime);

        var articleAuthor = userService.register("test_username1", "test_password");
        var commentAuthor = userService.register("test_username2", "test_password");
        var article = articleService.create(articleAuthor.getId(), "test_title", "test_body");
        var expectedComment = "コメントしました";

        // ## Act ##
        var actual = cut.create(commentAuthor.getId(), article.getId(), expectedComment);

        // ## Assert ##
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getBody()).isEqualTo(expectedComment);
        assertThat(actual.getAuthor())
                .usingRecursiveComparison()
                .ignoringFields("password")
                .isEqualTo(commentAuthor);
        assertThat(actual.getCreatedAt()).isEqualTo(expectedCurrentDateTime);
    }

    @Test
    @DisplayName("create: 指定された記事IDが存在しないとき、ResourceNotFoundException を投げる")
    void create_articleDoesNotExist() {
        // ## Arrange ##
        var expectedCurrentDateTime = TestDateTimeUtil.of(2020, 1, 2, 10, 20, 30);
        when(mockDateTimeService.now()).thenReturn(expectedCurrentDateTime);

        var commentAuthor = userService.register("test_username2", "test_password");

        // ## Act ##
        // ## Assert ##
        assertThrows(ResourceNotFoundException.class, () -> {
            cut.create(commentAuthor.getId(), 0, "test_comment_body");
        });
    }

    @Test
    @DisplayName("findByArticleId: 記事IDを指定して記事コメントの一覧が取得できる")
    void findByArticleId_success() {
        // ## Arrange ##
        when(mockDateTimeService.now())
                .thenReturn(TestDateTimeUtil.of(2020, 1, 2, 10, 20, 30))
                .thenReturn(TestDateTimeUtil.of(2021, 1, 2, 10, 20, 30))
                .thenReturn(TestDateTimeUtil.of(2022, 1, 2, 10, 20, 30))
        ;

        var articleAuthor = userService.register("test_username1", "test_password");
        var article = articleService.create(articleAuthor.getId(), "test_title", "test_body");

        var commentAuthor1 = userService.register("test_username2", "test_password");
        var comment1 = cut.create(commentAuthor1.getId(), article.getId(), "test_comment_body1");

        var commentAuthor2 = userService.register("test_username3", "test_password");
        var comment2 = cut.create(commentAuthor2.getId(), article.getId(), "test_comment_body1");

        // ## Act ##
        var actual = cut.findByArticleId(article.getId());

        // ## Assert ##
        assertThat(actual).hasSize(2);
        assertThat(actual.get(0)).isEqualTo(comment1);
        assertThat(actual.get(1)).isEqualTo(comment2);
    }
}