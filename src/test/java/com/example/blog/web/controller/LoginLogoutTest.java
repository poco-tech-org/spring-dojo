package com.example.blog.web.controller;


import com.example.blog.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LoginLogoutTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserService userService;

    @Test
    void mockMvc() {
        assertThat(mockMvc).isNotNull();
    }

    @Test
    @DisplayName("POST /login: ログイン成功")
    void login_success() throws Exception {
        // ## Arrange ##
        var username = "username123";
        var password = "password123";
        userService.register(username, password);

        var newUserJson = """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(username, password);

        // ## Act ##
        var actual = mockMvc.perform(
                post("/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newUserJson)
        );

        // ## Assert ##
        actual.andExpect(status().isOk());
    }
}
