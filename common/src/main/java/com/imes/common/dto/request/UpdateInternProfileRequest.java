package com.imes.common.dto.request;

import jakarta.validation.constraints.*;

public record UpdateInternProfileRequest(
        @Email(message = "Email không hợp lệ")
        String email,

        String fullName,

        @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại phải có 10-11 chữ số")
        String phoneNumber,

        String major,

        String university,

        @DecimalMin(value = "0.0", message = "GPA phải >= 0.0")
        @DecimalMax(value = "4.0", message = "GPA phải <= 4.0")
        Double gpa,

        String skills,

        String status
) {
}
