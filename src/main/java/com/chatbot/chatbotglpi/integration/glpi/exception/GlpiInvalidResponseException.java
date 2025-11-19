package com.chatbot.chatbotglpi.integration.glpi.exception;

import com.chatbot.chatbotglpi.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Exceção lançada quando a resposta da API do GLPI é inválida ou vazia
 */
public class GlpiInvalidResponseException extends BusinessException {

    private static final String ERROR_CODE = "GLPI_INVALID_RESPONSE";

    public GlpiInvalidResponseException(String message) {
        super(ERROR_CODE, message);
    }

    public GlpiInvalidResponseException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_GATEWAY;
    }
}
