package com.example.blog.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(classes = StorageService.class)
class StorageServiceTest {

    @Autowired
    private StorageService cut;

    @Test
    void test() {
        assertThat(cut).isNotNull();
    }
}