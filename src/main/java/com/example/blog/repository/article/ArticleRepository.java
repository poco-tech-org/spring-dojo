package com.example.blog.repository.article;

import com.example.blog.service.article.ArticleEntity;
import org.apache.ibatis.annotations.*;

import java.util.Optional;

@Mapper
public interface ArticleRepository {

    @Select("""
            SELECT
                id
              , title
              , body
              , created_at
              , updated_at
            FROM articles
            WHERE id = #{id}
            """)
    @Results(value = {
            @Result(column = "id", property = "id"),
            @Result(column = "title", property = "title"),
            @Result(column = "body", property = "body"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt")
    })
    Optional<ArticleEntity> selectById(@Param("id") long id);

    @Insert("""
            INSERT INTO articles (user_id, title, body, create_at, updated_at)
            VALUES (#{author.id}, #{title}, #{body}, #{createdAt}, #{updatedAt});
            """)
    void insert(ArticleEntity entity);
}
