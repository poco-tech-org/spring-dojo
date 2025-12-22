package com.example.blog.repository.user;

import com.example.blog.service.user.UserEntity;
import org.apache.ibatis.annotations.*;

import java.util.Optional;

@Mapper
public interface UserRepository {

    default Optional<UserEntity> selectByUsername(String username) {
        return Optional.ofNullable(username)
                .flatMap(this::selectByUsernameInternal);
    }

    @Select("""
            SELECT
                id
              , u.username
              , u.password
              , u.enabled
              , u.image_path AS imagePath
            FROM users u
            WHERE u.username = #{username}
            """)
    Optional<UserEntity> selectByUsernameInternal(@Param("username") String username);


    @Insert("""
            INSERT INTO users (username, password, enabled, image_path)
            VALUES (#{username}, #{password}, #{enabled}, #{imagePath})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insert(UserEntity entity);

    @Delete("""
            DELETE FROM users u
            WHERE u.username = #{username}
            """)
    void deleteByUsername(@Param("username") String username);

    @Update("""
            UPDATE users
            SET
                password   = #{password}
              , enabled    = #{enabled}
              , image_path = #{imagePath}
            WHERE
                id = #{id}
            """)
    void update(UserEntity entity);
}
