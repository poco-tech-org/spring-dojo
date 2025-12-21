package com.example.blog.web.controller.user;

import com.example.blog.api.UsersApi;
import com.example.blog.model.UserDTO;
import com.example.blog.model.UserForm;
import com.example.blog.model.UserProfileImageForm;
import com.example.blog.model.UserProfileImageUploadURLDTO;
import com.example.blog.security.LoggedInUser;
import com.example.blog.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequiredArgsConstructor
public class UserRestController implements UsersApi {

    private final UserService userService;
    private final DuplicateUsernameValidator duplicateUsernameValidator;

    @InitBinder("userForm")
    public void initBinder(DataBinder dataBinder) {
        dataBinder.addValidators(duplicateUsernameValidator);
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

    @Override
    public ResponseEntity<UserProfileImageUploadURLDTO> getProfileImageUploadURL(
            String fileName,
            String contentType,
            Long contentLength
    ) {
        var loggedInUser = (LoggedInUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        var uploadURL = userService.createProfileImageUploadURL(
                loggedInUser,
                fileName,
                contentType,
                contentLength
        );
        var dto = new UserProfileImageUploadURLDTO()
                .imagePath(uploadURL.imagePath())
                .imageUploadUrl(uploadURL.uploadURL());
        return ResponseEntity.ok(dto);
    }

    @Override
    public ResponseEntity<UserDTO> updateUserProfileImage(
            UserProfileImageForm userProfileImageForm
    ) {
        var loggedInUser = (LoggedInUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        var updatedUser = userService.updateProfileImage(
                loggedInUser.getUsername(),
                userProfileImageForm.getImagePath()
        );
        var userDTO = new UserDTO()
                .id(updatedUser.getId())
                .username(updatedUser.getUsername());
        // TODO set profile image
        return ResponseEntity.ok(userDTO);
    }

    @Override
    public ResponseEntity<UserDTO> getCurrentUser() {
        var loggedInUser = (LoggedInUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        var userDTO = new UserDTO()
                .id(loggedInUser.getUserId())
                .username(loggedInUser.getUsername())
                .imagePath("dummy");
        return ResponseEntity.ok(userDTO);
    }
}
