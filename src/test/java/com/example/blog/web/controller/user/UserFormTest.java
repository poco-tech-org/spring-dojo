package com.example.blog.web.controller.user;

import com.example.blog.model.UserForm;
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

    @ParameterizedTest
    @DisplayName("username のバリデーション：成功")
    @ValueSource(strings = {
            "aaa",
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
            "abcdefghijklmnopqrstuvwxyz",
            "0123456789",
            "user-._name",
    })
    void username_success(String username) {
        // ## Arrange ##
        var cut = new UserForm(username, "password00");

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
            ".username",
            "-username",
            "_username",
            "username.",
            "username-",
            "username_",
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

    @ParameterizedTest
    @DisplayName("password のバリデーション：成功")
    @ValueSource(strings = {
            // 10 characters
            "1234567890",
            // 255 characters
            "12345678901234567890123456789012345678901234567890"
            + "12345678901234567890123456789012345678901234567890"
            + "12345678901234567890123456789012345678901234567890"
            + "12345678901234567890123456789012345678901234567890"
            + "12345678901234567890123456789012345678901234567890"
            + "12345",
            // alphabet + symbol
            "~!@#$%^&*()_+QWERTYUIOP{}|ASDFGHJKL:ZXCVBNM<>?"
    })
    void password_success(String password) {
        // ## Arrange ##
        var cut = new UserForm("username00", password);

        // ## Act ##
        var actual = validator.validate(cut);

        // ## Assert ##
        assertThat(actual).isEmpty();
    }

    @ParameterizedTest
    @DisplayName("password のバリデーション：失敗")
    @ValueSource(strings = {
            // 9 characters
            "123456789",
            // 256 characters
            "12345678901234567890123456789012345678901234567890"
                    + "12345678901234567890123456789012345678901234567890"
                    + "12345678901234567890123456789012345678901234567890"
                    + "12345678901234567890123456789012345678901234567890"
                    + "12345678901234567890123456789012345678901234567890"
                    + "123456",
    })
    void password_failure(String password) {
        // ## Arrange ##
        var cut = new UserForm("username00", password);

        // ## Act ##
        var actual = validator.validate(cut);

        // ## Assert ##
        assertThat(actual).isNotEmpty();
        assertThat(actual)
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("password"));
    }
}
