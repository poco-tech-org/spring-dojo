package com.example.blog.web.controller.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserRestControllerTest {

    private static final String MOCK_USERNAME = "user1";

    @Autowired
    private MockMvc mockMvc;

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

}