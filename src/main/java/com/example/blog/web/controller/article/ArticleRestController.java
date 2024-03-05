package com.example.blog.web.controller.article;

import com.example.blog.service.article.ArticleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class ArticleRestController {

    private final ArticleService articleService = new ArticleService();

    @GetMapping("/articles/{id}")
    public ArticleDTO showArticle(@PathVariable("id") long id) {
        var entity = articleService.findById(id);
        return new ArticleDTO(
                entity.id(),
                entity.title(),
                entity.content(),
                entity.createdAt(),
                entity.updatedAt()
        );
    }
}
