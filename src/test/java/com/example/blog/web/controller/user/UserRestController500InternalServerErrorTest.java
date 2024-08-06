package com.example.blog.web.controller.user;


import com.example.blog.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserRestController500InternalServerErrorTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;

    @Test
    void setUp_success() {
        // ## Arrange ##

        // ## Act ##

        // ## Assert ##
        assertThat(mockMvc).isNotNull();
        assertThat(userService).isNotNull();
    }

    @Test
    @DisplayName("POST /users: 500 InternalServerError で stacktrace が露出しない")
    void createUsers_500() throws Exception {
        // ## Arrange ##
        var username = "username123";
        var password = "password123";
        when(userService.register(username, password)).thenThrow(RuntimeException.class);

        var newUserJson = """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(username, password);

        // ## Act ##
        var actual = mockMvc.perform(
                post("/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newUserJson)
        );

        // ## Assert ##
        actual
                .andExpect(status().isInternalServerError())
        ;
    }
}
