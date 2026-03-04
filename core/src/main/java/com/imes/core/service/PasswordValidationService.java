package com.imes.core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Password Validation Service
 * Validates password strength and common password patterns
 */
@Slf4j
@Service
public class PasswordValidationService {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 128;

    // Regex patterns
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*(),.?\":{}|<>_\\-+=\\[\\]\\\\;'/`~]");

    // Common weak passwords (subset for demo)
    private static final List<String> COMMON_PASSWORDS = Arrays.asList(
            "password", "123456", "12345678", "qwerty", "abc123", "monkey",
            "1234567", "letmein", "trustno1", "dragon", "baseball", "iloveyou",
            "master", "sunshine", "ashley", "bailey", "shadow", "123123",
            "admin", "admin123", "password123", "welcome", "login", "passw0rd"
    );

    /**
     * Validate password strength
     * @param password Password to validate
     * @return Validation result with error message if invalid
     */
    public PasswordValidationResult validate(String password) {
        if (password == null || password.isEmpty()) {
            return PasswordValidationResult.invalid("Password cannot be empty");
        }

        if (password.length() < MIN_LENGTH) {
            return PasswordValidationResult.invalid(
                    String.format("Password must be at least %d characters long", MIN_LENGTH)
            );
        }

        if (password.length() > MAX_LENGTH) {
            return PasswordValidationResult.invalid(
                    String.format("Password must not exceed %d characters", MAX_LENGTH)
            );
        }

        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            return PasswordValidationResult.invalid(
                    "Password must contain at least one uppercase letter"
            );
        }

        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            return PasswordValidationResult.invalid(
                    "Password must contain at least one lowercase letter"
            );
        }

        if (!DIGIT_PATTERN.matcher(password).find()) {
            return PasswordValidationResult.invalid(
                    "Password must contain at least one digit"
            );
        }

        if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            return PasswordValidationResult.invalid(
                    "Password must contain at least one special character (!@#$%^&*...)"
            );
        }

        // Check against common passwords
        String lowerPassword = password.toLowerCase();
        if (COMMON_PASSWORDS.contains(lowerPassword)) {
            return PasswordValidationResult.invalid(
                    "This password is too common. Please choose a more unique password"
            );
        }

        // Check for sequential characters (e.g., "12345", "abcde")
        if (hasSequentialCharacters(password)) {
            return PasswordValidationResult.invalid(
                    "Password should not contain sequential characters"
            );
        }

        return PasswordValidationResult.valid();
    }

    /**
     * Check if password contains sequential characters
     */
    private boolean hasSequentialCharacters(String password) {
        for (int i = 0; i < password.length() - 2; i++) {
            char c1 = password.charAt(i);
            char c2 = password.charAt(i + 1);
            char c3 = password.charAt(i + 2);

            // Check for ascending sequence (e.g., "123", "abc")
            if (c2 == c1 + 1 && c3 == c2 + 1) {
                return true;
            }

            // Check for descending sequence (e.g., "321", "cba")
            if (c2 == c1 - 1 && c3 == c2 - 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculate password strength score (0-100)
     */
    public int calculateStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }

        int score = 0;

        // Length score (max 30 points)
        score += Math.min(password.length() * 2, 30);

        // Character variety score
        if (UPPERCASE_PATTERN.matcher(password).find()) score += 15;
        if (LOWERCASE_PATTERN.matcher(password).find()) score += 15;
        if (DIGIT_PATTERN.matcher(password).find()) score += 15;
        if (SPECIAL_CHAR_PATTERN.matcher(password).find()) score += 15;

        // Complexity bonus
        long uniqueChars = password.chars().distinct().count();
        score += (int) (uniqueChars / (double) password.length() * 10);

        return Math.min(score, 100);
    }

    /**
     * Password validation result
     */
    public static class PasswordValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private PasswordValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static PasswordValidationResult valid() {
            return new PasswordValidationResult(true, null);
        }

        public static PasswordValidationResult invalid(String errorMessage) {
            return new PasswordValidationResult(false, errorMessage);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
