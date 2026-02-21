package com.imes.common.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateInternProfileRequest(
        @Email(message = "Email không hợp lệ")
        String email,

        @Pattern(regexp = "^[A-Z0-9]{6,20}$", message = "Mã sinh viên phải là 6-20 ký tự, chữ in hoa và số")
        String studentId,

        String fullName,

        @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại phải có 10-11 chữ số")
        String phoneNumber,

        String major,

        String university,

        @DecimalMin(value = "0.0", message = "GPA phải >= 0.0")
        @DecimalMax(value = "4.0", message = "GPA phải <= 4.0")
        BigDecimal gpa,

        String skills,

        LocalDate startDate,

        LocalDate endDate,

        Long mentorId,

        Long departmentId,

        String avatarUrl,

        String status
) {
}
