package com.example.blog.web.controller.article;

import com.example.blog.model.ArticleCommentDTO;
import com.example.blog.model.UserDTO;
import com.example.blog.service.article.ArticleCommentEntity;
import org.springframework.beans.BeanUtils;

public class ArticleCommentMapper {
    public static ArticleCommentDTO toDTO(ArticleCommentEntity entity) {
        var commentDTO = new ArticleCommentDTO();
        BeanUtils.copyProperties(entity, commentDTO);

        var userDTO = new UserDTO();
        BeanUtils.copyProperties(entity.getAuthor(), userDTO);
        commentDTO.setAuthor(userDTO);

        return commentDTO;
    }
}
