package com.chatbot.chatbotglpi.conversation.application.port.input;

import com.chatbot.chatbotglpi.conversation.api.dto.request.ValidatedMessage;

/**
 * Port de entrada para validação de mensagens.
 * SRP - responsabilidade única de validar entrada.
 */
public interface MessageValidationPort {

    ValidatedMessage validate(String phone, String message);
}
