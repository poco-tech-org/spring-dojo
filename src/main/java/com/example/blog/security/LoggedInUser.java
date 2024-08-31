package com.example.blog.security;

import lombok.Getter;
import org.springframework.security.core.userdetails.User;

import java.util.List;

@Getter
public class LoggedInUser extends User {

    private final long userId;

    public LoggedInUser(
            long userId,
            String username,
            String password,
            boolean enabled
    ) {
        super(username, password, enabled, true, true, true, List.of());
        this.userId = userId;
    }
}
