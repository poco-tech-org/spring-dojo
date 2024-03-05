package com.example.blog.web.controller.article;

import com.example.blog.service.article.ArticleEntity;

import java.time.LocalDateTime;

public record ArticleDTO(
        long id,
        String title,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ArticleDTO from(ArticleEntity entity) {
        return new ArticleDTO(
                entity.id(),
                entity.title(),
                entity.content(),
                entity.createdAt(),
                entity.updatedAt()
        );
    }
}
