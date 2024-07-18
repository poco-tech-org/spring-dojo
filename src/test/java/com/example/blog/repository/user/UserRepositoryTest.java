package com.example.blog.repository.user;

import com.example.blog.config.MybatisDefaultDatasourceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisDefaultDatasourceTest
class UserRepositoryTest {

    @Autowired
    private UserRepository cut;

    @Test
    void successAutowired() {
        assertThat(cut).isNotNull();
    }
}