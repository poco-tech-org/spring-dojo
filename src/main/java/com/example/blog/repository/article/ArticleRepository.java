package com.example.blog.repository.article;

import com.example.blog.service.article.ArticleEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ArticleRepository {

    @Select("""
            SELECT
                a.id         AS article__id
              , a.title      AS article__title
              , a.body       AS article__body
              , a.created_at AS article__created_at
              , a.updated_at AS article__updated_at
              , u.id         AS user__id
              , u.username   AS user__username
              , u.enabled    AS user__enabled
            FROM articles a
            JOIN users u ON a.user_id = u.id
            WHERE a.id = #{articleId}
            """)
    @Results(value = {
            @Result(column = "article__id", property = "id"),
            @Result(column = "article__title", property = "title"),
            @Result(column = "article__body", property = "body"),
            @Result(column = "article__created_at", property = "createdAt"),
            @Result(column = "article__updated_at", property = "updatedAt"),

            @Result(column = "user__id", property = "author.id"),
            @Result(column = "user__username", property = "author.username"),
            @Result(column = "user__enabled", property = "author.enabled"),
    })
    Optional<ArticleEntity> selectById(@Param("articleId") long articleId);

    @Insert("""
            INSERT INTO articles (user_id, title, body, created_at, updated_at)
            VALUES (#{author.id}, #{title}, #{body}, #{createdAt}, #{updatedAt});
            """)
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    void insert(ArticleEntity entity);

    @Select("""
            SELECT
                a.id         AS article__id
              , a.title      AS article__title
              , a.body       AS article__body
              , a.created_at AS article__created_at
              , a.updated_at AS article__updated_at
              , u.id         AS user__id
              , u.username   AS user__username
              , u.enabled    AS user__enabled
            FROM articles a
            JOIN users u ON a.user_id = u.id
            ORDER BY a.created_at DESC
            """)
    @Results(value = {
            @Result(column = "article__id", property = "id"),
            @Result(column = "article__title", property = "title"),
            @Result(column = "article__body", property = "body"),
            @Result(column = "article__created_at", property = "createdAt"),
            @Result(column = "article__updated_at", property = "updatedAt"),

            @Result(column = "user__id", property = "author.id"),
            @Result(column = "user__username", property = "author.username"),
            @Result(column = "user__enabled", property = "author.enabled"),
    })
    List<ArticleEntity> selectAll();
}
