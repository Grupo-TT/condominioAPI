package com.condominio.util.exception;

import com.condominio.dto.response.ErrorResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {



    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResult> handleApiException(ApiException ex) {
        ErrorResult error = new ErrorResult(ex.getMessage(), ex.getStatus().value());
        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResult> handleGeneral(Exception ex) {
        ErrorResult error = new ErrorResult("Error interno: " + ex.getMessage(), 500);
        return ResponseEntity.status(500).body(error);
    }

}
