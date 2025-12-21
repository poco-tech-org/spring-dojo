package com.example.blog.service.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserEntity {
    private Long id;
    private String username;
    private String password;
    private boolean enabled;
    private String imagePath;

    // TODO 後で直す
    public UserEntity(Long id, String username, String password, boolean enabled) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.enabled = enabled;
    }
}
