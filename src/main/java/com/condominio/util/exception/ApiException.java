package com.condominio.util.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
    private final HttpStatus status;

    public ApiException(String description, HttpStatus status) {
        super(description);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
