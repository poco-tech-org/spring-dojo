package com.example.blog.web.controller.article;

import com.example.blog.model.ArticleDTO;
import com.example.blog.model.ArticleListItemDTO;
import com.example.blog.model.UserDTO;
import com.example.blog.service.article.ArticleEntity;
import org.springframework.beans.BeanUtils;

public class ArticleMapper {

    public static ArticleDTO toArticleDTO(ArticleEntity entity) {
        var userDTO = new UserDTO();
        BeanUtils.copyProperties(entity.getAuthor(), userDTO);

        var articleDTO = new ArticleDTO();
        BeanUtils.copyProperties(entity, articleDTO);
        articleDTO.setAuthor(userDTO);

        return articleDTO;
    }

    public static ArticleListItemDTO toArticleListItemDTO(ArticleEntity entity) {
        var userDto = new UserDTO();
        BeanUtils.copyProperties(entity.getAuthor(), userDto);

        var itemDto = new ArticleListItemDTO();
        BeanUtils.copyProperties(entity, itemDto);
        itemDto.setAuthor(userDto);

        return itemDto;
    }
}
