package com.imes.core.exception;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ClientSideException extends RuntimeException {

    private final ErrorCode code;
    private final String message;
    private final Object[] messageArgs;
    private final Object data;

    public ClientSideException(ErrorCode code) {
        super();
        this.code = code;
        this.message = code.getDefaultMessage();
        this.messageArgs = null;
        this.data = null;
    }

    public ClientSideException(ErrorCode code, String message) {
        super(message);
        this.code = code;
        this.message = message;
        this.messageArgs = null;
        this.data = null;
    }

    public ClientSideException(ErrorCode code, String message, Object[] messageArgs) {
        super(message);
        this.code = code;
        this.message = message;
        this.messageArgs = messageArgs;
        this.data = null;
    }

    public ClientSideException(ErrorCode code, String message, Object data) {
        super(message);
        this.code = code;
        this.message = message;
        this.messageArgs = null;
        this.data = data;
    }

    @Builder
    private ClientSideException(
            ErrorCode code, String message, Object[] messageArgs, Object data) {
        super(message);
        this.code = code;
        this.message = message;
        this.messageArgs = messageArgs;
        this.data = data;
    }
}
