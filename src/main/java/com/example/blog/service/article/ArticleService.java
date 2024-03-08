package com.example.blog.service.article;

import com.example.blog.repository.article.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;

    public Optional<ArticleEntity> findById(long id) {
        return articleRepository.selectById(id);
    }
}
