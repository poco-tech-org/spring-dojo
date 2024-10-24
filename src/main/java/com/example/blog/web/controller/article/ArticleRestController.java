package com.example.blog.web.controller.article;

import com.example.blog.api.ArticlesApi;
import com.example.blog.model.*;
import com.example.blog.security.LoggedInUser;
import com.example.blog.service.article.ArticleCommentService;
import com.example.blog.service.article.ArticleService;
import com.example.blog.service.exception.ResourceNotFoundException;
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
    private final ArticleCommentService articleCommentService;

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

        var body = ArticleMapper.toArticleDTO(newArticle);
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
                .map(ArticleMapper::toArticleListItemDTO)
                .toList();

        var body = new ArticleListDTO();
        body.setItems(items);

        return ResponseEntity
                .ok(body);
    }

    @Override
    public ResponseEntity<ArticleDTO> getArticle(Long articleId) {
        return articleService.findById(articleId)
                .map(ArticleMapper::toArticleDTO)
                .map(ResponseEntity::ok)
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

        var entity = articleService.update(
                loggedInUser.getUserId(),
                articleId,
                form.getTitle(),
                form.getBody()
        );

        return ResponseEntity
                .ok(ArticleMapper.toArticleDTO(entity));
    }

    @Override
    public ResponseEntity<Void> deleteArticle(Long articleId) {
        var loggedInUser = (LoggedInUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        articleService.delete(loggedInUser.getUserId(), articleId);

        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ArticleCommentDTO> createComment(
            Long articleId,
            ArticleCommentForm form
    ) {
        var loggedInUser = (LoggedInUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        var newComment = articleCommentService.create(
                loggedInUser.getUserId(),
                articleId,
                form.getBody()
        );

        var body = ArticleCommentMapper.toDTO(newComment);
        var location = UriComponentsBuilder
                .fromPath("/articles/{articleId}/comments/{commentId}")
                .buildAndExpand(articleId, newComment.getId())
                .toUri();

        return ResponseEntity
                .created(location)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    @Override
    public ResponseEntity<ArticleCommentListDTO> listArticleComments(Long articleId) {
        var commentList = articleCommentService.findByArticleId(articleId)
                .stream()
                .map(ArticleCommentMapper::toDTO)
                .toList();

        var body = new ArticleCommentListDTO();
        body.comments(commentList);

        return ResponseEntity
                .ok(body);
    }
}
