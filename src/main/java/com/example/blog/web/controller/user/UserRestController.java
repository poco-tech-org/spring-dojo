package com.example.blog.web.controller.user;

import com.example.blog.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserRestController {

    private final UserService userService;

    // GET /users/me
    @GetMapping("/me")
    public ResponseEntity<String> me(Principal principal) {
        return ResponseEntity.ok(principal.getName());
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody UserForm userForm) {
        var newUser = userService.register(userForm.username(), userForm.password());
        return ResponseEntity
                .created(URI.create("/users/" + newUser.getId()))
                .build();
    }
}
