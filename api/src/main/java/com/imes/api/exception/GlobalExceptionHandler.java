package com.imes.api.exception;

import com.imes.common.dto.FieldError;
import com.imes.common.dto.ResponseApi;
import com.imes.core.exception.ClientSideException;
import com.imes.core.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ClientSideException.class)
    public ResponseEntity<ResponseApi<?>> handleClientSideException(
            ClientSideException ex, WebRequest request) {
        log.warn("Client side exception: [{}] {}", ex.getCode().getCode(), ex.getMessage());
        ResponseApi<?> response = ResponseApi.error(ex.getCode().getCode(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseApi<?>> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        List<FieldError> fieldErrors = new ArrayList<>();
        BindingResult bindingResult = ex.getBindingResult();
        
        for (ObjectError error : bindingResult.getAllErrors()) {
            String fieldName = "";
            if (error instanceof org.springframework.validation.FieldError) {
                fieldName = ((org.springframework.validation.FieldError) error).getField();
            }
            String message = error.getDefaultMessage();
            fieldErrors.add(new FieldError(fieldName, message));
        }
        
        log.warn("Validation error: {} field(s) failed validation", fieldErrors.size());
        ResponseApi<?> response = ResponseApi.error(
                ErrorCode.VALIDATION_ERROR.getCode(),
                "Validation error",
                fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseApi<?>> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        ResponseApi<?> response = ResponseApi.error(
                ErrorCode.FORBIDDEN.getCode(),
                "Access Denied - You don't have permission to access this resource");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseApi<?>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        log.warn("Illegal argument: {}", ex.getMessage());
        ResponseApi<?> response = ResponseApi.error(
                ErrorCode.BAD_REQUEST.getCode(),
                ex.getMessage() != null ? ex.getMessage() : "Invalid argument");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseApi<?>> handleGeneralException(
            Exception ex, WebRequest request) {
        log.error("Unexpected error occurred", ex);
        ResponseApi<?> response = ResponseApi.error(
                ErrorCode.SYSTEM_ERROR.getCode(),
                "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
