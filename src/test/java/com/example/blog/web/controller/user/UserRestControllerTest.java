package com.example.blog.web.controller.user;

import com.example.blog.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserRestControllerTest {

    private static final String MOCK_USERNAME = "user1";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserService userService;

    @Test
    public void mockMvc() {
        assertThat(mockMvc).isNotNull();
    }

    @Test
    @DisplayName("/users/me: ログイン済みユーザーがアクセスすると、200 OK でユーザー名を返す")
    @WithMockUser(username = MOCK_USERNAME)
    public void usersMe_return200() throws Exception {
        // ## Arrange ##

        // ## Act ##
        var actual = mockMvc.perform(MockMvcRequestBuilders.get("/users/me"));

        // ## Assert ##
        actual
                .andExpect(status().isOk())
                .andExpect(content().bytes(MOCK_USERNAME.getBytes()));

    }

    @Test
    @DisplayName("/users/me: 未ログインユーザーがアクセスすると、403 Forbidden を返す")
    public void usersMe_return203() throws Exception {
        // ## Arrange ##

        // ## Act ##
        var actual = mockMvc.perform(MockMvcRequestBuilders.get("/users/me"));

        // ## Assert ##
        actual.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /users: ユーザー作成に成功すると、レスポンスボディにユーザー情報/LocationヘッダーにURIがセットされる")
    void createUser_success() throws Exception {
        // ## Arrange ##
        var newUserJson = """
                {
                  "username": "username123",
                  "password": "password123"
                }
                """;

        // ## Act ##
        var actual = mockMvc.perform(
                post("/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newUserJson)
        );

        // ## Assert ##
        actual
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/users/\\d+")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.username").value("username123"))
                .andExpect(jsonPath("$.password").doesNotExist())
        ;
    }

    @Test
    @DisplayName("POST /users: リクエストボディに username のキーがないとき、400 Bad Request")
    void createUser_badRequest() throws Exception {
        // ## Arrange ##
        var newUserJson = """
                {
                  "password": "password123"
                }
                """;

        // ## Act ##
        var actual = mockMvc.perform(
                post("/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newUserJson)
        );

        // ## Assert ##
        actual
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("POST /users: すでに登録されているユーザー名を指定したとき、400 Bad Request")
    void createUser_badRequest_duplicateUsername() throws Exception {
        // ## Arrange ##
        var duplicateUsername = "username00";
        userService.register(duplicateUsername, "test_password");

        var newUserJson = """
                {
                  "username": "%s",
                  "password": "password123"
                }
                """.formatted(duplicateUsername);

        // ## Act ##
        var actual = mockMvc.perform(
                post("/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newUserJson)
        );

        // ## Assert ##
        actual
                .andExpect(status().isBadRequest())
        ;
    }

}