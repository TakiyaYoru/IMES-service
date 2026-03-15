package com.imes.attendance.exception;

import com.imes.common.dto.ApiResponse;
import com.imes.core.exception.ClientSideException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ClientSideException.class)
    public ResponseEntity<ApiResponse<?>> handleClientSideException(ClientSideException ex) {
        HttpStatus status = ex.getCode() != null && "0404".equals(ex.getCode().getCode())
                ? HttpStatus.NOT_FOUND
                : HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status)
                .body(ApiResponse.error(
                        ex.getCode() != null ? ex.getCode().getCode() : "0400",
                        ex.getMessage() != null ? ex.getMessage() : "Bad request"
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getDefaultMessage() != null ? err.getDefaultMessage() : "Validation error")
                .orElse("Validation error");

        return ResponseEntity.badRequest().body(ApiResponse.error("3001", message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error("3002", ex.getMessage()));
    }
}
