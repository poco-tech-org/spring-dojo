package com.example.blog.web.controller.user;

import com.example.blog.model.UserForm;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class UserFormTest {

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

    @Test
    @DisplayName("username のバリデーション：成功")
    void username_success() {
        // ## Arrange ##
        var cut = new UserForm("username00", "password00");

        // ## Act ##
        var actual = validator.validate(cut);

        // ## Assert ##
        assertThat(actual).isEmpty();
    }

    @ParameterizedTest
    @DisplayName("username のバリデーション：失敗")
    @NullSource
    @ValueSource(strings = {
            "",
            "a",
            "aa",
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
            "username!",
            "Username",
    })
    void username_failure(String username) {
        // ## Arrange ##
        var cut = new UserForm(username, "password00");

        // ## Act ##
        var actual = validator.validate(cut);

        // ## Assert ##
        assertThat(actual).isNotEmpty();
        assertThat(actual)
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("username"));
    }
}
