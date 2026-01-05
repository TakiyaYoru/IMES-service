package com.imes.common.dto;

import java.time.Instant;

public record ResponseMeta(String requestId, long timestamp) {

    public static ResponseMeta fromRequestId(String requestId) {
        return new ResponseMeta(requestId, Instant.now().toEpochMilli());
    }
}
