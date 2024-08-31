package com.example.blog.service.article;

import com.example.blog.service.user.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ArticleEntity {
    private Long id;
    private String title;
    private String body;
    private UserEntity author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
