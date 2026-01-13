package com.imes.common.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateInternProfileRequest(
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        String email,

        @NotBlank(message = "Họ tên không được để trống")
        String fullName,

        @NotBlank(message = "Số điện thoại không được để trống")
        @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại phải có 10-11 chữ số")
        String phoneNumber,

        @NotBlank(message = "Ngành học không được để trống")
        String major,

        @NotBlank(message = "Trường đại học không được để trống")
        String university,

        @NotNull(message = "GPA không được để trống")
        @DecimalMin(value = "0.0", message = "GPA phải >= 0.0")
        @DecimalMax(value = "4.0", message = "GPA phải <= 4.0")
        BigDecimal gpa,

        @NotBlank(message = "Kỹ năng không được để trống")
        String skills
) {
}
