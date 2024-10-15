package com.example.blog.repository.article;

import com.example.blog.service.article.ArticleCommentEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface ArticleCommentRepository {

    @Insert("""
            INSERT INTO article_comments(body, user_id, article_id, created_at)
            VALUES (#{body}, #{author.id}, #{article.id}, #{createdAt});
            """)
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    void insert(ArticleCommentEntity entity);

}
