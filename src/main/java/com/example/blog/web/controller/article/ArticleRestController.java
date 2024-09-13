package com.example.blog.web.controller.article;

import com.example.blog.api.ArticlesApi;
import com.example.blog.model.*;
import com.example.blog.security.LoggedInUser;
import com.example.blog.service.article.ArticleService;
import com.example.blog.web.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
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
        var items = articleService.findAll()
                .stream()
                .map(entity -> {
                    var itemDto = new ArticleListItemDTO();
                    BeanUtils.copyProperties(entity, itemDto);

                    var userDto = new UserDTO();
                    BeanUtils.copyProperties(entity.getAuthor(), userDto);

                    itemDto.setAuthor(userDto);

                    return itemDto;
                })
                .toList();

        var body = new ArticleListDTO();
        body.setItems(items);

        return ResponseEntity
                .ok(body);
    }

    @Override
    public ResponseEntity<ArticleDTO> getArticle(Long articleId) {
        return articleService.findById(articleId)
                .map(entity -> {
                    var userDTO = new UserDTO();
                    BeanUtils.copyProperties(entity.getAuthor(), userDTO);

                    var body = new ArticleDTO();
                    BeanUtils.copyProperties(entity, body);
                    body.setAuthor(userDTO);

                    return ResponseEntity.ok(body);
                })
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public ResponseEntity<ArticleDTO> updateArticle(
            Long articleId,
            ArticleForm form
    ) {
        var loggedInUser = (LoggedInUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return articleService.update(
                loggedInUser.getUserId(),
                articleId,
                form.getTitle(),
                form.getBody()
        ).map(updatedEntity -> {
            var userDTO = new UserDTO();
            BeanUtils.copyProperties(updatedEntity.getAuthor(), userDTO);
            var body = new ArticleDTO();
            BeanUtils.copyProperties(updatedEntity, body);
            body.setAuthor(userDTO);

            return ResponseEntity.ok(body);
        }).orElseThrow(ResourceNotFoundException::new);
    }
}
