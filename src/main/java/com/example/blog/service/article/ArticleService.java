package com.example.blog.service.article;

import com.example.blog.repository.article.ArticleRepository;
import com.example.blog.service.user.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;

    public Optional<ArticleEntity> findById(long id) {
        return articleRepository.selectById(id);
    }

    @Transactional
    public ArticleEntity create(long userId, String title, String body) {
        var timestamp = OffsetDateTime.now();
        var newEntity = new ArticleEntity(
                null,
                title,
                body,
                new UserEntity(userId, null, null, true),
                timestamp,
                timestamp
        );
        articleRepository.insert(newEntity);
        return newEntity;
    }
}
