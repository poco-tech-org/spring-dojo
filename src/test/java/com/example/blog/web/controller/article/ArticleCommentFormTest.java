package com.example.blog.web.controller.article;

import com.example.blog.model.ArticleCommentForm;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ArticleCommentFormTest {

    private ValidatorFactory validatorFactory;
    private Validator validator;

    @BeforeEach
    void beforeEach() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterEach
    void afterEach() {
        validatorFactory.close();
    }

    @ParameterizedTest
    @DisplayName("body のバリデーション：成功")
    @ValueSource(strings = {
            // 1文字
            "あ",

            // 長い文字列（256文字）
            "あいうえおかきくけこさしすせそたちつてとなにぬねの"
                    + "あいうえおかきくけこさしすせそたちつてとなにぬねの"
                    + "あいうえおかきくけこさしすせそたちつてとなにぬねの"
                    + "あいうえおかきくけこさしすせそたちつてとなにぬねの"
                    + "あいうえおかきくけこさしすせそたちつてとなにぬねの"
                    + "あいうえおかきくけこさしすせそたちつてとなにぬねの"
                    + "あいうえおかきくけこさしすせそたちつてとなにぬねの"
                    + "あいうえおかきくけこさしすせそたちつてとなにぬねの"
                    + "あいうえおかきくけこさしすせそたちつてとなにぬねの"
                    + "あいうえおかきくけこさしすせそたちつてとなにぬねの"
                    + "あいうえおか"
    })
    void body_success(String body) {
        // ## Arrange ##
        var cut = new ArticleCommentForm(body);

        // ## Act ##
        var actual = validator.validate(cut);

        // ## Assert ##
        assertThat(actual).isEmpty();
    }

    @ParameterizedTest
    @DisplayName("body のバリデーション：失敗")
    @NullSource
    @ValueSource(strings = {
            ""
    })
    void body_failure(String body) {
        // ## Arrange ##
        var cut = new ArticleCommentForm(body);

        // ## Act ##
        var actual = validator.validate(cut);

        // ## Assert ##
        assertThat(actual).isNotEmpty();
        assertThat(actual)
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("body"));
    }
}
