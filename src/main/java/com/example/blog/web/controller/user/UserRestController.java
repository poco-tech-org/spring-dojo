package com.example.blog.web.controller.user;

import com.example.blog.api.UsersApi;
import com.example.blog.model.BadRequest;
import com.example.blog.model.ErrorDetail;
import com.example.blog.model.UserDTO;
import com.example.blog.model.UserForm;
import com.example.blog.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.DataBinder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.Principal;
import java.util.ArrayList;

@RestController
@RequiredArgsConstructor
public class UserRestController implements UsersApi {

    private final UserService userService;
    private final DuplicateUsernameValidator duplicateUsernameValidator;
    private final MessageSource messageSource;

    @InitBinder
    public void initBinder(DataBinder dataBinder) {
        dataBinder.addValidators(duplicateUsernameValidator);
    }

    // GET /users/me
    @GetMapping("/users/me")
    public ResponseEntity<String> me(Principal principal) {
        return ResponseEntity.ok(principal.getName());
    }

    @Override
    public ResponseEntity<UserDTO> createUser(UserForm userForm) {
        var newUser = userService.register(userForm.getUsername(), userForm.getPassword());
        var location = UriComponentsBuilder.fromPath("/users/{id}")
                .buildAndExpand(newUser.getId())
                .toUri();
        var dto = new UserDTO();
        dto.setId(newUser.getId());
        dto.setUsername(newUser.getUsername());
        return ResponseEntity
                .created(location)
                .body(dto);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BadRequest> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e
    ) {
        var body = new BadRequest();
        BeanUtils.copyProperties(e.getBody(), body);

        var locale = LocaleContextHolder.getLocale();
        var errorDetailList = new ArrayList<ErrorDetail>();
        for (final FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            var pointer = "#/" + fieldError.getField();
            var detail = messageSource.getMessage(fieldError, locale);

            var errorDetail = new ErrorDetail();
            errorDetail.setPointer(pointer);
            errorDetail.setDetail(detail);

            errorDetailList.add(errorDetail);
        }
        body.setErrors(errorDetailList);
        return ResponseEntity
                .badRequest()
                .body(body);
    }

}
