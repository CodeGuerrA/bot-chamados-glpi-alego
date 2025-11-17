package com.chatbot.chatbotglpi.conversation.domain.service;

import com.chatbot.chatbotglpi.conversation.application.port.input.DescriptionValidatorPort;
import com.chatbot.chatbotglpi.shared.config.ChatbotProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Domain Service para validação de descrição.
 * Contém regras de negócio para descrições de tickets.
 */
@Service
@RequiredArgsConstructor
public class DescriptionValidatorService implements DescriptionValidatorPort {

    private final ChatbotProperties chatbotProperties;

    @Override
    public ValidationResult validate(String description) {
        if (description == null || description.isBlank()) {
            return ValidationResult.invalid("Descrição não pode ser vazia.");
        }

        int minLength = chatbotProperties.getValidation().getDescription().getMinLength();
        int maxLength = chatbotProperties.getValidation().getDescription().getMaxLength();

        if (description.length() < minLength) {
            return ValidationResult.invalid(
                    String.format("Descrição muito curta. Mínimo %d caracteres.", minLength)
            );
        }

        if (description.length() > maxLength) {
            return ValidationResult.invalid(
                    String.format("Descrição muito longa. Máximo %d caracteres.", maxLength)
            );
        }

        return ValidationResult.valid();
    }
}
