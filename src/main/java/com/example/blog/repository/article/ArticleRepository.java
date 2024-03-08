package com.example.blog.repository.article;

import com.example.blog.service.article.ArticleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
    Optional<ArticleEntity> selectById(@Param("id") long id);
}
