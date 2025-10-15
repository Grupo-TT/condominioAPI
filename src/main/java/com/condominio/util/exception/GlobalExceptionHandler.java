package com.condominio.util.exception;

import com.condominio.dto.response.ErrorResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

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
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResult> handleEnumConversionError(MethodArgumentTypeMismatchException ex) {
        if (ex.getRequiredType() != null && ex.getRequiredType().isEnum()) {
            String validValues = Arrays.stream(ex.getRequiredType().getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));

            String message = "El valor '" + ex.getValue() +
                    "' no es válido. Los estados permitidos son: " + validValues;

            ErrorResult error = new ErrorResult(message, HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        ErrorResult error = new ErrorResult("Parámetro inválido.", HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return ResponseEntity
                .status(403)
                .body(Map.of("timestamp", OffsetDateTime.now().toString(),
                        "status", 403,
                        "error", "Forbidden",
                        "message", "Acceso denegado: no tienes permisos suficientes",
                        "path", req.getRequestURI()));
    }
}
