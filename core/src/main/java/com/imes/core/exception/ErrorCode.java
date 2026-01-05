package com.imes.core.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // System errors
    BAD_REQUEST("0400", "SYSTEM", "Invalid request"),
    NOT_FOUND("0404", "SYSTEM", "Resource not found"),
    FORBIDDEN("0403", "SYSTEM", "Forbidden"),
    INVALID_SIGNATURE("0444", "SYSTEM", "Invalid signature"),
    SYSTEM_ERROR("0500", "SYSTEM", "System error"),

    // User errors
    USER_NOT_FOUND("1001", "USER", "User not found"),
    USER_ALREADY_EXISTS("1002", "USER", "User already exists"),
    INVALID_PASSWORD("1003", "USER", "Invalid password"),
    EMAIL_ALREADY_EXISTS("1004", "USER", "Email already exists"),
    INVALID_EMAIL("1005", "USER", "Invalid email format"),
    PASSWORD_MISMATCH("1006", "USER", "Passwords do not match"),
    INSUFFICIENT_PERMISSIONS("1007", "USER", "Insufficient permissions"),

    // Authentication errors
    INVALID_TOKEN("2001", "AUTH", "Invalid token"),
    TOKEN_EXPIRED("2002", "AUTH", "Token expired"),
    UNAUTHORIZED("2003", "AUTH", "Unauthorized"),
    INVALID_CREDENTIALS("2004", "AUTH", "Invalid credentials"),

    // Validation errors
    VALIDATION_ERROR("3001", "VALIDATION", "Validation error"),
    INVALID_INPUT("3002", "VALIDATION", "Invalid input"),
    MISSING_REQUIRED_FIELD("3003", "VALIDATION", "Missing required field");

    private final String code;
    private final String category;
    private final String defaultMessage;

    ErrorCode(String code, String category, String defaultMessage) {
        this.code = code;
        this.category = category;
        this.defaultMessage = defaultMessage;
    }
}
