package com.imes.assignment.exception;

import com.imes.common.dto.ResponseApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseApi<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(error -> error instanceof FieldError fieldError
                        ? fieldError.getField() + ": " + error.getDefaultMessage()
                        : error.getDefaultMessage())
                .orElse("Validation error");

        return ResponseEntity.badRequest()
                .body(ResponseApi.error("0400", message));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ResponseApi<Void>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.badRequest()
                .body(ResponseApi.error("0400", ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseApi<Void>> handleRuntimeException(RuntimeException ex) {
        String message = ex.getMessage() == null ? "Runtime error" : ex.getMessage();

        if (message.toLowerCase().contains("not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ResponseApi.error("0404", message));
        }

        if (message.toLowerCase().contains("not authorized")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResponseApi.error("0403", message));
        }

        return ResponseEntity.badRequest()
                .body(ResponseApi.error("0400", message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseApi<Void>> handleUnhandledException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseApi.error("0500", "Internal server error"));
    }
}
