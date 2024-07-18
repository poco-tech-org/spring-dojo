package com.example.blog.repository.user;

import com.example.blog.config.MybatisDefaultDatasourceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisDefaultDatasourceTest
class UserRepositoryTest {

    @Autowired
    private UserRepository cut;

    @Test
    void successAutowired() {
        assertThat(cut).isNotNull();
    }

    @Test
    @DisplayName("selectByUsername: 指定されたユーザー名のユーザーが存在するとき、Optional<UserEntity> を返す")
    @Sql(statements = {
            "INSERT INTO users (id, username, password, enabled) VALUES (999, 'test_user_1', 'test_user_1_pass', true);",
            "INSERT INTO users (id, username, password, enabled) VALUES (998, 'test_user_2', 'test_user_2_pass', true);"
    })
    void selectByUsername_success() {
        // ## Arrange ##

        // ## Act ##
        var actual = cut.selectByUsername("test_user_1");

        // ## Assert ##
        assertThat(actual).hasValueSatisfying(actualEntity -> {
            assertThat(actualEntity.id()).isEqualTo(999);
            assertThat(actualEntity.username()).isEqualTo("test_user_1");
            assertThat(actualEntity.password()).isEqualTo("test_user_1_pass");
            assertThat(actualEntity.enabled()).isTrue();
        });
    }

    @Test
    @DisplayName("selectByUsername: 指定されたユーザー名のユーザーが存在しないとき、Optional.empty を返す")
    @Sql(statements = {
            "INSERT INTO users (id, username, password, enabled) VALUES (999, 'test_user_1', 'test_user_1_pass', true);",
    })
    void selectByUsername_returnEmpty() {
        // ## Arrange ##

        // ## Act ##
        var actual = cut.selectByUsername("invalid_user");

        // ## Assert ##
        assertThat(actual).isEmpty();
    }
}