package com.imes.intern.exception;

import com.imes.common.dto.ApiResponse;
import com.imes.core.exception.ClientSideException;
import com.imes.core.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ClientSideException.class)
    public ResponseEntity<ApiResponse<Void>> handleClientSideException(ClientSideException ex) {
        ErrorCode errorCode = ex.getCode();
        HttpStatus status = resolveHttpStatus(errorCode);
        return ResponseEntity.status(status.value())
                .body(ApiResponse.error(errorCode.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(error -> error instanceof FieldError fieldError
                        ? fieldError.getField() + ": " + error.getDefaultMessage()
                        : error.getDefaultMessage())
                .orElse("Validation error");

        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ErrorCode.VALIDATION_ERROR.getCode(), message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnhandledException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "Internal server error"));
    }

    private HttpStatus resolveHttpStatus(ErrorCode errorCode) {
        if (errorCode == null) {
            return HttpStatus.BAD_REQUEST;
        }

        if (errorCode == ErrorCode.FORBIDDEN) {
            return HttpStatus.FORBIDDEN;
        }

        if (errorCode == ErrorCode.UNAUTHORIZED || errorCode == ErrorCode.INVALID_TOKEN || errorCode == ErrorCode.TOKEN_EXPIRED) {
            return HttpStatus.UNAUTHORIZED;
        }

        if (errorCode == ErrorCode.NOT_FOUND || errorCode.name().endsWith("_NOT_FOUND")) {
            return HttpStatus.NOT_FOUND;
        }

        return HttpStatus.BAD_REQUEST;
    }
}
