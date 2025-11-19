package com.chatbot.chatbotglpi.integration.glpi.exception;

import com.chatbot.chatbotglpi.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Exceção lançada quando há falha na criação de ticket no GLPI
 */
public class GlpiTicketCreationException extends BusinessException {

    private static final String ERROR_CODE = "GLPI_TICKET_CREATION_ERROR";

    public GlpiTicketCreationException(String message) {
        super(ERROR_CODE, message);
    }

    public GlpiTicketCreationException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_GATEWAY;
    }
}
