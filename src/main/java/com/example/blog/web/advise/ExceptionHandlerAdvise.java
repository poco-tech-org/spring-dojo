package com.example.blog.web.advise;

import com.example.blog.model.InternalServerError;
import com.example.blog.web.exception.ResourceNotFoundException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHandlerAdvise {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<InternalServerError> handleInternalServerError(RuntimeException e) {
        return ResponseEntity
                .internalServerError()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(new InternalServerError());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Void> handleResourceNotFoundException(ResourceNotFoundException e) {
        return ResponseEntity.notFound().build();
    }
}
