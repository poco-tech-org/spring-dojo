package com.example.blog.web.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexRestController {

    // GET /
    @GetMapping
    public ResponseEntity<Void> index() {
        return ResponseEntity
                .noContent()
                .build();
    }
}
