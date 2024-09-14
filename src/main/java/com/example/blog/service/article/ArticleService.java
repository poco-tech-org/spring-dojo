package com.example.blog.service.article;

import com.example.blog.repository.article.ArticleRepository;
import com.example.blog.service.DateTimeService;
import com.example.blog.service.exception.UnauthorizedResourceAccessException;
import com.example.blog.service.user.UserEntity;
import com.example.blog.service.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final DateTimeService dateTimeService;

    public Optional<ArticleEntity> findById(long id) {
        return articleRepository.selectById(id);
    }

    @Transactional
    public ArticleEntity create(long userId, String title, String body) {
        var timestamp = dateTimeService.now();
        var newEntity = new ArticleEntity(
                null,
                title,
                body,
                new UserEntity(userId, null, null, true),
                timestamp,
                timestamp
        );
        articleRepository.insert(newEntity);

        return articleRepository.selectById(newEntity.getId())
                .orElseThrow(() -> new IllegalStateException("never reached"));
    }

    public List<ArticleEntity> findAll() {
        return articleRepository.selectAll();
    }

    @Transactional
    public ArticleEntity update(
            long loggedInUserId,
            long articleId,
            String updatedTitle,
            String updatedBody
    ) {
        var entity = findById(articleId)
                .orElseThrow(ResourceNotFoundException::new);

        if (entity.getAuthor().getId() != loggedInUserId) {
            throw new UnauthorizedResourceAccessException();
        }

        entity.setTitle(updatedTitle);
        entity.setBody(updatedBody);
        entity.setUpdatedAt(dateTimeService.now());
        articleRepository.update(entity);

        return entity;
    }
}
