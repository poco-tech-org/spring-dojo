package com.example.blog.web.controller.user;


import com.example.blog.model.UserForm;
import com.example.blog.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class DuplicateUsernameValidator implements Validator {

    private final UserService userService;

    @Override
    public boolean supports(Class<?> clazz) {
        return UserForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        var form = (UserForm) target;
        if (userService.existsUsername(form.getUsername())) {
            errors.rejectValue("username", "duplicate.userForm.username");
        }
    }
}
