package com.example.blog.web.controller.article;

import com.example.blog.api.ArticlesApi;
import com.example.blog.model.ArticleDTO;
import com.example.blog.model.ArticleForm;
import com.example.blog.model.ArticleListDTO;
import com.example.blog.model.UserDTO;
import com.example.blog.security.LoggedInUser;
import com.example.blog.service.article.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequiredArgsConstructor
public class ArticleRestController implements ArticlesApi {

    private final ArticleService articleService;

    @Override
    public ResponseEntity<ArticleDTO> createArticle(ArticleForm form) {
        var loggedInUser = (LoggedInUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        var newArticle = articleService.create(
                loggedInUser.getUserId(),
                form.getTitle(),
                form.getBody()
        );

        var userDTO = new UserDTO();
        userDTO.setId(newArticle.getAuthor().getId());
        userDTO.setUsername(newArticle.getAuthor().getUsername());

        var body = new ArticleDTO();
        body.setId(newArticle.getId());
        body.title(newArticle.getTitle());
        body.body(newArticle.getBody());
        body.setAuthor(userDTO);
        body.setCreatedAt(newArticle.getCreatedAt());
        body.setUpdatedAt(newArticle.getUpdatedAt());

        var location = UriComponentsBuilder.fromPath("/articles/{id}")
                .buildAndExpand(newArticle.getId())
                .toUri();

        return ResponseEntity
                .created(location)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    @Override
    public ResponseEntity<ArticleListDTO> listArticles() {
        var entitEyList = articleService.findAll();

        return ResponseEntity
                .ok(new ArticleListDTO());
    }
}
