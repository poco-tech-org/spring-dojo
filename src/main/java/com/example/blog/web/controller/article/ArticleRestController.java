package com.example.blog.web.controller.article;

import com.example.blog.service.article.ArticleService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestController
public class ArticleRestController {

    private final ArticleService articleService = new ArticleService();

    @GetMapping("/articles/{id}")
    public ArticleDTO showArticle(@PathVariable("id") long id) {
        return articleService.findById(id)
                .map(entity ->
                    new ArticleDTO(
                            entity.id(),
                            entity.title(),
                            entity.content(),
                            entity.createdAt(),
                            entity.updatedAt()
                    )
                )
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
