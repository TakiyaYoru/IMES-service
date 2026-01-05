package com.imes.common.dto;

import java.util.List;

public record ResponseStatus(String code, String message, List<FieldError> errors) {
    public ResponseStatus(String code, String message) {
        this(code, message, null);
    }

    public ResponseStatus(String code) {
        this(code, null, null);
    }
}
