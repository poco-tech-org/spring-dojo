package com.example.blog.web.controller;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.core.StringContains.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CSRFCookieRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /csrf-cookie: 204 を返し、Set-Cookie ヘッダに XSRF-TOKEN が含まれている")
    void return204() throws Exception {
        // ## Arrange ##

        // ## Act ##
        var result = mockMvc.perform(get("/csrf-cookie"));

        // ## Assert ##
        result
                .andExpect(status().isNoContent())
                .andExpect(header().string("Set-Cookie", containsString("XSRF-TOKEN")));
    }

}