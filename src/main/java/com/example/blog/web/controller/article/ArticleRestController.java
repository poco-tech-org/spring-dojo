package com.example.blog.web.controller.article;

import com.example.blog.api.ArticlesApi;
import com.example.blog.service.article.ArticleService;
import com.example.blog.web.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class ArticleRestController implements ArticlesApi {

    private final ArticleService articleService;

    @GetMapping("/articles/{id}")
    public ArticleDTO showArticle(@PathVariable("id") long id) {
        return articleService.findById(id)
                .map(ArticleDTO::from)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public ResponseEntity<Void> createArticle() {
        return ResponseEntity.created(URI.create("about:blank")).build();
    }
}
