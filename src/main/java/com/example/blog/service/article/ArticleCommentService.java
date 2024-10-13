package com.example.blog.service.article;

import com.example.blog.service.user.UserEntity;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class ArticleCommentService {

    public ArticleCommentEntity create(
            long userId,
            long articleId,
            @NotNull String body
    ) {
        return new ArticleCommentEntity(
                null,
                body,
                new ArticleEntity(articleId, "", "", null, null, null),
                new UserEntity(userId, "", "", true),
                OffsetDateTime.now()
        );
    }
}
