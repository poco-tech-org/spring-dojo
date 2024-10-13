package com.example.blog.service.article;

import com.example.blog.service.user.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleCommentEntity {

    private Long id;
    private String body;
    private ArticleEntity article;
    private UserEntity author;
    private OffsetDateTime createdAt;

}
