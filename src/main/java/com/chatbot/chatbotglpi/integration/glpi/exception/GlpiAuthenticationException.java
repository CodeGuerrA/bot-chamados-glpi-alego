package com.chatbot.chatbotglpi.integration.glpi.exception;

import com.chatbot.chatbotglpi.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Exceção lançada quando há falha na autenticação com a API do GLPI
 */
public class GlpiAuthenticationException extends BusinessException {

    private static final String ERROR_CODE = "GLPI_AUTH_ERROR";

    public GlpiAuthenticationException(String message) {
        super(ERROR_CODE, message);
    }

    public GlpiAuthenticationException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_GATEWAY;
    }
}
