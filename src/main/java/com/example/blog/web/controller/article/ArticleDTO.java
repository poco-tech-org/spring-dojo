package com.example.blog.web.controller.article;

import java.time.LocalDateTime;

public record ArticleDTO(
        long id,
        String title,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
