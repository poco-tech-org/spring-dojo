package com.example.blog.service.article;

import java.time.LocalDateTime;

public record ArticleEntity(
        Long id,
        String title,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
