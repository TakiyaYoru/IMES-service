package com.imes.common.dto;

import java.util.Collections;
import java.util.List;
import org.slf4j.MDC;

public record ApiResponse<T>(ResponseStatus status, T data, ResponseMeta metaData) {
    private static final String SUCCESS = "0000";
    private static final String SUCCESS_MESSAGE = "Success";
    public static final String X_REQUEST_ID = "X-Request-ID";

    private ApiResponse(ResponseStatus status, ResponseMeta metaData) {
        this(status, null, metaData);
    }

    public static <T> ApiResponse<T> success() {
        ResponseStatus status = new ResponseStatus(SUCCESS);
        ResponseMeta meta = ResponseMeta.fromRequestId(MDC.get(X_REQUEST_ID));
        return success(status, null, meta);
    }

    public static <T> ApiResponse<T> success(ResponseStatus status, T payload) {
        ResponseMeta meta = ResponseMeta.fromRequestId(MDC.get(X_REQUEST_ID));
        return success(status, payload, meta);
    }

    public static <T> ApiResponse<T> success(T payload) {
        return success(new ResponseStatus(SUCCESS, SUCCESS_MESSAGE), payload);
    }

    public static <T> ApiResponse<T> success(T payload, ResponseMeta meta) {
        return new ApiResponse<>(new ResponseStatus(SUCCESS), payload, meta);
    }

    public static <T> ApiResponse<T> success(ResponseStatus status, T payload, ResponseMeta meta) {
        if (meta == null) {
            meta = ResponseMeta.fromRequestId(MDC.get(X_REQUEST_ID));
        }
        return new ApiResponse<>(status, payload, meta);
    }

    public static <T> ApiResponse<T> error(String errorCode, String errorMessage) {
        return error(errorCode, errorMessage, Collections.emptyList());
    }

    public static <T> ApiResponse<T> error(
            String errorCode, String errorMessage, List<FieldError> fieldErrors) {
        ResponseStatus responseStatus = new ResponseStatus(errorCode, errorMessage, fieldErrors);
        ResponseMeta meta = ResponseMeta.fromRequestId(MDC.get(X_REQUEST_ID));
        return new ApiResponse<>(responseStatus, null, meta);
    }

    public static <T> ApiResponse<T> error(ResponseStatus status) {
        ResponseMeta meta = ResponseMeta.fromRequestId(MDC.get(X_REQUEST_ID));
        return new ApiResponse<>(status, null, meta);
    }

    public static <T> ApiResponse<T> error(ResponseStatus status, T payload, ResponseMeta meta) {
        if (meta == null) {
            meta = ResponseMeta.fromRequestId(MDC.get(X_REQUEST_ID));
        }
        return new ApiResponse<>(status, payload, meta);
    }
}
