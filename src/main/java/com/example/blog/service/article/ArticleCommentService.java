package com.example.blog.service.article;

import com.example.blog.repository.article.ArticleCommentRepository;
import com.example.blog.repository.article.ArticleRepository;
import com.example.blog.service.DateTimeService;
import com.example.blog.service.exception.ResourceNotFoundException;
import com.example.blog.service.user.UserEntity;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleCommentService {

    private final ArticleCommentRepository articleCommentRepository;
    private final ArticleRepository articleRepository;
    private final DateTimeService dateTimeService;

    public ArticleCommentEntity create(
            long userId,
            long articleId,
            @NotNull String body
    ) {
        articleRepository.selectById(articleId)
                .orElseThrow(ResourceNotFoundException::new);

        var newComment = new ArticleCommentEntity(
                null,
                body,
                new ArticleEntity(articleId, "", "", null, null, null),
                new UserEntity(userId, "", "", true),
                dateTimeService.now()
        );
        articleCommentRepository.insert(newComment);

        return articleCommentRepository
                .selectById(newComment.getId())
                .orElseThrow(() -> new IllegalStateException("never reached"));
    }

    public List<ArticleCommentEntity> findByArticleId(long articleId) {
        return articleCommentRepository.selectByArticleId(articleId);
    }
}
