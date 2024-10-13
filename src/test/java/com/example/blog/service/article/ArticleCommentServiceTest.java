package com.example.blog.service.article;

import com.example.blog.config.MybatisDefaultDatasourceTest;
import com.example.blog.config.PasswordEncoderConfig;
import com.example.blog.service.DateTimeService;
import com.example.blog.service.user.UserService;
import com.example.blog.util.TestDateTimeUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
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
}