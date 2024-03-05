package com.example.blog.service.article;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ArticleService {

    public Optional<ArticleEntity> findById(long id) {
        return Optional.of(new ArticleEntity(
                id,
                "title",
                "content",
                LocalDateTime.now(),
                LocalDateTime.now()
        ));
    }
}
