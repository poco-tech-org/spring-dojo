package com.example.blog.web.controller.article;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class ArticleRestController {

    @GetMapping("/articles/{id}")
    public ArticleDTO showArticle(@PathVariable("id") long id) {
        return new ArticleDTO(
                id,
                "This is title: id = " + id,
                "This is content",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
