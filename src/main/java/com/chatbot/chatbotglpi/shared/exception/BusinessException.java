package com.chatbot.chatbotglpi.shared.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

@Getter
public abstract class BusinessException extends RuntimeException {

    private final String errorCode;
    private final Map<String, Object> metadata = new HashMap<>();

    protected BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public abstract HttpStatus getHttpStatus();

    public void addMetadata(String key, Object value) {
        if (value != null) {
            metadata.put(key, value);
        }
    }
}
