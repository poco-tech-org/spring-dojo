package com.example.blog.web.advise;

import com.example.blog.model.InternalServerError;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHandlerAdvise {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<InternalServerError> handleInternalServerError(RuntimeException e) {
        var error = new InternalServerError();
        error.setType(null);
        error.title("Internal Server Error");
        error.status(500);
        error.setDetail(null);
        error.setInstance(null);

        return ResponseEntity.internalServerError().body(error);
    }
}
