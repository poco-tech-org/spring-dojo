package com.example.blog.repository.user;

import com.example.blog.config.MybatisDefaultDatasourceTest;
import com.example.blog.service.user.UserEntity;
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
            "INSERT INTO users (id, username, password, enabled, image_path) VALUES (999, 'test_user_1', 'test_user_1_pass', true, 'users/999/profile-image');",
            "INSERT INTO users (id, username, password, enabled, image_path) VALUES (998, 'test_user_2', 'test_user_2_pass', true, 'users/998/profile-image');"
    })
    void selectByUsername_success() {
        // ## Arrange ##

        // ## Act ##
        var actual = cut.selectByUsername("test_user_1");

        // ## Assert ##
        assertThat(actual).hasValueSatisfying(actualEntity -> {
            assertThat(actualEntity.getId()).isEqualTo(999);
            assertThat(actualEntity.getUsername()).isEqualTo("test_user_1");
            assertThat(actualEntity.getPassword()).isEqualTo("test_user_1_pass");
            assertThat(actualEntity.isEnabled()).isTrue();
            assertThat(actualEntity.getImagePath()).isEqualTo("users/999/profile-image");
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

    @Test
    @DisplayName("selectByUsername: ユーザー名にnullが指定されたとき、Optional.empty を返す")
    @Sql(statements = {
            "INSERT INTO users (id, username, password, enabled) VALUES (999, 'null', 'test_user_1_pass', true);",
    })
    void selectByUsername_returnEmpty_whenNullIsGiven() {
        // ## Arrange ##

        // ## Act ##
        var actual = cut.selectByUsername(null);

        // ## Assert ##
        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("insert: User を登録することができる。image_path カラムはユーザー登録時には未設定")
    void insert_success() {
        // ## Arrange ##
        var newRecord = new UserEntity(null, "test_user_1", "test_user_1_pass", true, null);

        // ## Act ##
        cut.insert(newRecord);

        // ## Assert ##
        assertThat(newRecord.getId())
                .describedAs("AUTO INCREMENT で設定された id が entity の id フィールドに設定されている")
                .isGreaterThanOrEqualTo(1);

        var actual = cut.selectByUsername("test_user_1");
        assertThat(actual).hasValueSatisfying(actualEntity -> {
            assertThat(actualEntity.getId()).isGreaterThanOrEqualTo(1);
            assertThat(actualEntity.getUsername()).isEqualTo("test_user_1");
            assertThat(actualEntity.getPassword()).isEqualTo("test_user_1_pass");
            assertThat(actualEntity.isEnabled()).isTrue();
            assertThat(actualEntity.getImagePath()).isNull();
        });
    }

    @Test
    @DisplayName("update: UserEntity を更新することができる")
    void update_success() {
        // ## Arrange ##
        var existingUser1 = new UserEntity(1L, "user_1", "password_1", true, "users/1/profile-image");
        var existingUser2 = new UserEntity(2L, "user_2", "password_2", true, "users/2/profile-image");
        cut.insert(existingUser1);
        cut.insert(existingUser2);

        var userToUpdate = new UserEntity(
                existingUser1.getId(),
                existingUser1.getUsername(),
                existingUser1.getPassword() + "_updated",
                !existingUser1.isEnabled(),
                existingUser1.getImagePath() + "_updated"
        );

        // ## Act ##
        cut.update(userToUpdate);

        // ## Assert ##
        assertThat(cut.selectByUsername(existingUser1.getUsername()))
                .as("指定したユーザーが更新されている")
                .contains(userToUpdate);
        assertThat(cut.selectByUsername(existingUser2.getUsername()))
                .as("指定していないユーザーは更新されない")
                .contains(existingUser2);
    }

    @Test
    @DisplayName("update: 更新対象が存在しないときは insert/update がされず、エラーも発生しない")
    void update_nonExistentUser() {
        // ## Arrange ##
        var existingUser1 = new UserEntity(1L, "user_1", "password_1", true, null);
        cut.insert(existingUser1);

        var nonExistenceUser = new UserEntity(
                999L, // 存在しないユーザーID
                "dummy_username",
                "dummy_password",
                false,
                null
        );

        // ## Act ##
        cut.update(nonExistenceUser);

        // ## Assert ##
        assertThat(cut.selectByUsername(existingUser1.getUsername()))
                .as("指定していないユーザーは更新されない")
                .contains(existingUser1);
        assertThat(cut.selectByUsername(nonExistenceUser.getUsername()))
                .as("指定しないユーザーに update をかけても insert されない")
                .isEmpty();
    }
}