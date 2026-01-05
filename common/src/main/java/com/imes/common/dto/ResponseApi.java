package com.imes.common.dto;

import java.util.Collections;
import java.util.List;
import org.slf4j.MDC;

public record ResponseApi<T>(ResponseStatus status, T data, ResponseMeta metaData) {
    private static final String SUCCESS = "0000";
    private static final String SUCCESS_MESSAGE = "Success";
    public static final String X_REQUEST_ID = "X-Request-ID";

    private ResponseApi(ResponseStatus status, ResponseMeta metaData) {
        this(status, null, metaData);
    }

    public static <T> ResponseApi<T> success() {
        ResponseStatus status = new ResponseStatus(SUCCESS);
        ResponseMeta meta = ResponseMeta.fromRequestId(MDC.get(X_REQUEST_ID));
        return success(status, null, meta);
    }

    public static <T> ResponseApi<T> success(ResponseStatus status, T payload) {
        ResponseMeta meta = ResponseMeta.fromRequestId(MDC.get(X_REQUEST_ID));
        return success(status, payload, meta);
    }

    public static <T> ResponseApi<T> success(T payload) {
        return success(new ResponseStatus(SUCCESS, SUCCESS_MESSAGE), payload);
    }

    public static <T> ResponseApi<T> success(T payload, ResponseMeta meta) {
        return new ResponseApi<>(new ResponseStatus(SUCCESS), payload, meta);
    }

    public static <T> ResponseApi<T> success(ResponseStatus status, T payload, ResponseMeta meta) {
        if (meta == null) {
            meta = ResponseMeta.fromRequestId(MDC.get(X_REQUEST_ID));
        }
        return new ResponseApi<>(status, payload, meta);
    }

    public static <T> ResponseApi<T> error(String errorCode, String errorMessage) {
        return error(errorCode, errorMessage, Collections.emptyList());
    }

    public static <T> ResponseApi<T> error(
            String errorCode, String errorMessage, List<FieldError> fieldErrors) {
        ResponseStatus responseStatus = new ResponseStatus(errorCode, errorMessage, fieldErrors);
        ResponseMeta meta = ResponseMeta.fromRequestId(MDC.get(X_REQUEST_ID));
        return new ResponseApi<>(responseStatus, null, meta);
    }

    public static <T> ResponseApi<T> error(String errorCode, String errorMessage, T payload) {
        ResponseStatus responseStatus = new ResponseStatus(errorCode, errorMessage, null);
        ResponseMeta meta = ResponseMeta.fromRequestId(MDC.get(X_REQUEST_ID));
        return new ResponseApi<>(responseStatus, payload, meta);
    }
}
