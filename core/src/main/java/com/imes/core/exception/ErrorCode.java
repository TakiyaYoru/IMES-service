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
    MISSING_REQUIRED_FIELD("3003", "VALIDATION", "Missing required field"),
    
    // InternProfile errors
    INTERN_PROFILE_NOT_FOUND("4001", "INTERN", "Intern profile not found"),
    INTERN_ALREADY_EXISTS("4002", "INTERN", "Intern profile already exists"),
    
    // MentorAssignment errors
    ASSIGNMENT_NOT_FOUND("5001", "ASSIGNMENT", "Mentor assignment not found"),
    CANNOT_ASSIGN_INACTIVE_MENTOR("5002", "ASSIGNMENT", "Cannot assign inactive mentor"),
    CANNOT_ASSIGN_INACTIVE_INTERN("5003", "ASSIGNMENT", "Cannot assign inactive intern"),
    ASSIGNMENT_ALREADY_EXISTS("5004", "ASSIGNMENT", "Active assignment already exists for this intern"),
    
    // Attendance errors
    ATTENDANCE_NOT_FOUND("6001", "ATTENDANCE", "Attendance record not found"),
    DUPLICATE_CHECK_IN("6002", "ATTENDANCE", "Already checked in for this date"),
    CHECK_IN_REQUIRED("6003", "ATTENDANCE", "Must check in before checking out"),
    ALREADY_CHECKED_OUT("6004", "ATTENDANCE", "Already checked out for this attendance"),
    INVALID_CHECK_OUT_TIME("6005", "ATTENDANCE", "Check-out time must be after check-in time"),
    LEAVE_ALREADY_REQUESTED("6006", "ATTENDANCE", "Leave already requested for this date"),
    
    // Task/Assignment Workflow errors
    TASK_NOT_FOUND("7001", "TASK", "Task/Assignment not found"),
    TASK_INVALID_DEADLINE("7002", "TASK", "Task deadline must be in the future"),
    TASK_ALREADY_EXISTS("7003", "TASK", "Task with this title already exists"),
    TASK_UNAUTHORIZED("7004", "TASK", "Unauthorized to modify this task"),
    TASK_INSTANCE_DUPLICATE("7005", "TASK_INSTANCE", "Task already assigned to this intern"),
    TASK_INSTANCE_NOT_FOUND("7006", "TASK_INSTANCE", "Task instance not found"),
    SUBMISSION_DEADLINE_PASSED("7007", "SUBMISSION", "Submission deadline has passed"),
    SUBMISSION_ALREADY_EXISTS("7008", "SUBMISSION", "Submission already exists for this task"),
    SUBMISSION_NOT_FOUND("7009", "SUBMISSION", "Submission not found"),
    SUBMISSION_NOT_SUBMITTED_YET("7010", "SUBMISSION", "Submission has not been submitted yet");

    private final String code;
    private final String category;
    private final String defaultMessage;

    ErrorCode(String code, String category, String defaultMessage) {
        this.code = code;
        this.category = category;
        this.defaultMessage = defaultMessage;
    }
}
