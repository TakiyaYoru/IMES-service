package com.imes.assignment.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record TimeLogListResponse(
        List<TimeLogResponse> items,
        BigDecimal totalHours
) {
}
