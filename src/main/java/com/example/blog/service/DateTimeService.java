package com.example.blog.service;

import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class DateTimeService {

    public OffsetDateTime now() {
        return OffsetDateTime.now();
    }
}
