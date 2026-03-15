package com.imes.common.dto.response;

public record PunctualityDistributionResponse(
        Integer onTimeCount,
        Integer lateCount,
        Double onTimePercentage,
        Double latePercentage
) {
}
