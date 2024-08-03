package com.example.blog.web.controller.user;

import com.example.blog.model.UserForm;
import jakarta.validation.Validation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserFormTest {

    @Test
    @DisplayName("username のバリデーション：成功")
    void username_success() {
        // ## Arrange ##
        var validatorFactory = Validation.buildDefaultValidatorFactory();
        var validator = validatorFactory.getValidator();
        var cut = new UserForm("username00", "password00");

        // ## Act ##
        var actual = validator.validate(cut);

        // ## Assert ##
        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("username のバリデーション：失敗")
    void username_failure() {
        // ## Arrange ##
        var validatorFactory = Validation.buildDefaultValidatorFactory();
        var validator = validatorFactory.getValidator();
        var cut = new UserForm(null, "password00");

        // ## Act ##
        var actual = validator.validate(cut);

        // ## Assert ##
        assertThat(actual).isNotEmpty();
    }
}
