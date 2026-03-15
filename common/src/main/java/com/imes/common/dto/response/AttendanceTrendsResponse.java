package com.imes.common.dto.response;

import java.util.List;

public record AttendanceTrendsResponse(
        String granularity,
        List<AttendanceTrendPointResponse> points
) {
}
