package com.example.blog.service.user;

import com.example.blog.config.MybatisDefaultDatasourceTest;
import com.example.blog.config.PasswordEncoderConfig;
import com.example.blog.config.S3PresignerConfig;
import com.example.blog.config.S3Properties;
import com.example.blog.repository.file.FileRepository;
import com.example.blog.repository.user.UserRepository;
import com.example.blog.security.LoggedInUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisDefaultDatasourceTest
@Import({
        UserService.class,
        PasswordEncoderConfig.class,
        FileRepository.class,
        S3PresignerConfig.class,
})
@EnableConfigurationProperties(S3Properties.class)
class UserServiceTest {

    @Autowired
    private UserService cut;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ApplicationContext ctx;

    @Test
    void successAutowired() {
        assertThat(cut).isNotNull();
        System.out.println("bean.length = " + ctx.getBeanDefinitionNames().length);
    }

    @Test
    @DisplayName("register：ユーザーがデータベースに登録される")
    void register_success() {
        // ## Arrange ##
        var username = "test_username";
        var password = "test_password";

        // ## Act ##
        cut.register(username, password);

        // ## Assert ##
        var actual = userRepository.selectByUsername(username);
        assertThat(actual).hasValueSatisfying(actualEntity -> {
            assertThat(actualEntity.getPassword())
                    .describedAs("入力された生のパスワードがハッシュ化されていること")
                    .isNotEmpty()
                    .isNotEqualTo(password);
            assertThat(actualEntity.isEnabled())
                    .describedAs("ユーザー登録時には、有効なアカウントとして登録する")
                    .isTrue();
        });
    }

    @Test
    @DisplayName("existsUsername: ユーザー名がすでに登録済みのとき true")
    void existsUsername_returnTrue() {
        // ## Arrange ##
        var username = "test_username";
        var alreadyExistUser = new UserEntity(null, username, "test_password", true);
        userRepository.insert(alreadyExistUser);

        // ## Act ##
        var actual = cut.existsUsername(username);

        // ## Assert ##
        assertThat(actual).isTrue();
    }

    @Test
    @DisplayName("existsUsername: ユーザー名が未登録のとき false")
    void existsUsername_returnFalse() {
        // ## Arrange ##
        var alreadyExistUser = new UserEntity(null, "dummy_username", "test_password", true);
        userRepository.insert(alreadyExistUser);

        // ## Act ##
        var actual = cut.existsUsername("new_username");

        // ## Assert ##
        assertThat(actual).isFalse();
    }

    @Test
    @DisplayName("createProfileImageUploadURL: プロフィール画像登録の URL が生成されること")
    void createProfileImageUploadURL_success() {
        // ## Arrange ##
        var loggedInUser = new LoggedInUser(
                123L,
                "test_username",
                "test_password",
                true
        );

        // ## Act ##
        var actual = cut.createProfileImageUploadURL(
                loggedInUser,
                "test.png",
                "image/png",
                1024
        );

        // ## Assert ##
        assertThat(actual).isNotNull();
        assertThat(actual.imagePath())
                .isEqualTo(
                        "users/%d/profile-image.png".formatted(loggedInUser.getUserId())
                );
        assertThat(actual.uploadURL()).isNotNull();
    }

    @Test
    @DisplayName("delete: 存在するユーザーを削除できること")
    void delete_success_existingUser() {
        // ## Arrange ##
        var existingUser1 = new UserEntity(null, "test_username1", "test_password", true);
        userRepository.insert(existingUser1);

        var existingUser2 = new UserEntity(null, "test_username2", "test_password", true);
        userRepository.insert(existingUser2);

        // ## Act ##
        cut.delete(existingUser1.getUsername());

        // ## Assert ##
        assertThat(userRepository.selectByUsername(existingUser1.getUsername()))
                .isEmpty();
        assertThat(userRepository.selectByUsername(existingUser2.getUsername()))
                .contains(existingUser2);
    }

    @Test
    @DisplayName("delete: 存在しないユーザーを削除しても例外が発生せずに処理が完了し、全件削除にならないこと")
    void delete_success_nonExistingUser() {
        // ## Arrange ##
        var existingUser1 = new UserEntity(null, "test_username1", "test_password", true);
        userRepository.insert(existingUser1);

        // ## Act ##
        cut.delete("non_existing_username");

        // ## Assert ##
        assertThat(userRepository.selectByUsername(existingUser1.getUsername()))
                .contains(existingUser1);
    }

    @Test
    @DisplayName("delete: nullのユーザー名を指定しても全件削除にならないこと")
    void delete_withNullUsername() {
        // ## Arrange ##
        var existingUser1 = new UserEntity(null, "test_username1", "test_password", true);
        userRepository.insert(existingUser1);

        // ## Act ##
        cut.delete(null);

        // ## Assert ##
        assertThat(userRepository.selectByUsername(existingUser1.getUsername()))
                .contains(existingUser1);
    }

    @Test
    @DisplayName("delete: 空文字のユーザー名を指定しても全件削除にならないこと")
    void delete_withEmptyUsername() {
        // ## Arrange ##
        var existingUser1 = new UserEntity(null, "test_username1", "test_password", true);
        userRepository.insert(existingUser1);

        // ## Act ##
        cut.delete(null);

        // ## Assert ##
        assertThat(userRepository.selectByUsername(existingUser1.getUsername()))
                .contains(existingUser1);
    }
}