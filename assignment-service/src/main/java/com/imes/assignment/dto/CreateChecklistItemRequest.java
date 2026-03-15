package com.imes.assignment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateChecklistItemRequest(
        @NotBlank(message = "Item text is required")
        @Size(max = 255, message = "Item text must not exceed 255 characters")
        String itemText
) {
}
