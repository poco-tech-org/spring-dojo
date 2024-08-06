package com.example.blog.web.controller;


import com.example.blog.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class LoginLogoutTest {

    private MockMvc mockMvc;
    @Autowired
    private UserService userService;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void beforeEach() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

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
        actual
                .andExpect(status().isOk())
                .andExpect(authenticated().withUsername(username))
        ;
    }

    @ParameterizedTest
    @DisplayName("POST /login: ログイン失敗")
    @ValueSource(strings = {
            "{ \"username\": \"__invalid__\", \"password\": \"password123\" }",
            "{ \"username\": \"username123\", \"password\": \"__invalid__\" }",
            "{                                \"password\": \"password123\" }",
            "{ \"username\": \"username123\",                               }",
            "{}",
            "",
    })
    void login_failure(String requestBody) throws Exception {
        // ## Arrange ##
        userService.register("username123", "password123");

        // ## Act ##
        var actual = mockMvc.perform(
                post("/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        );

        // ## Assert ##
        actual
                .andExpect(status().isUnauthorized())
                .andExpect(unauthenticated())
        ;
    }
}
