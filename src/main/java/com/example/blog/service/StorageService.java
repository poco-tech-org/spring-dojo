package com.example.blog.service;

import org.springframework.stereotype.Service;

@Service
public class StorageService {

    public String createUploadURL(
            String fileName,
            String contentType,
            Long contentLength
    ) {
        return "dummy";
    }
}
