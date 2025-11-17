package com.chatbot.chatbotglpi.conversation.application.service;

import com.chatbot.chatbotglpi.conversation.api.dto.request.ValidatedMessage;
import com.chatbot.chatbotglpi.conversation.application.port.input.MessageValidationPort;
import com.chatbot.chatbotglpi.shared.util.InputSanitizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementação do serviço de validação de mensagens.
 * SRP - única responsabilidade: validar e sanitizar entrada.
 */
@Slf4j
@Service
public class MessageValidationService implements MessageValidationPort {

    @Override
    public ValidatedMessage validate(String phone, String message) {
        log.debug("Validando mensagem de {}", phone);

        // Sanitiza e valida telefone
        String cleanPhone = InputSanitizer.cleanPhone(phone);
        if (!InputSanitizer.isValidPhone(cleanPhone)) {
            log.warn("Telefone inválido recebido: {}", phone);
            return ValidatedMessage.invalid("Número de telefone inválido");
        }

        // Sanitiza e valida mensagem
        String sanitizedMessage = InputSanitizer.sanitize(message);
        if (sanitizedMessage == null || sanitizedMessage.trim().isEmpty()) {
            log.warn("Mensagem vazia recebida de {}", cleanPhone);
            return ValidatedMessage.invalid("Mensagem inválida. Por favor, envie uma mensagem válida.");
        }

        return ValidatedMessage.valid(cleanPhone, sanitizedMessage);
    }
}
