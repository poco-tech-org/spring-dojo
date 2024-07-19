package com.example.blog.service.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class UserEntity {
    private Long id;
    private String username;
    private String password;
    private boolean enabled;
}
