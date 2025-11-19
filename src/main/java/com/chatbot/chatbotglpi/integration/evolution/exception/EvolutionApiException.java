package com.chatbot.chatbotglpi.integration.evolution.exception;

import com.chatbot.chatbotglpi.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Exceção lançada quando há falha na comunicação com a Evolution API
 */
public class EvolutionApiException extends BusinessException {

    private static final String ERROR_CODE = "EVOLUTION_API_ERROR";

    public EvolutionApiException(String message) {
        super(ERROR_CODE, message);
    }

    public EvolutionApiException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_GATEWAY;
    }
}
