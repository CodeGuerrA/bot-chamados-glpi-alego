package com.chatbot.chatbotglpi.conversation.domain.validator.ramal;

import com.chatbot.chatbotglpi.conversation.domain.validator.base.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RamalValidator implements Validator<String> {

    @Override
    public ValidationResult validate(String ramal) {
        if (ramal == null || ramal.isBlank()) {
            return ValidationResult.invalid("Ramal não pode ser vazio.");
        }

        // Verifica se contém apenas números e tem tamanho razoável (ex: 3-6 dígitos)
        if (!ramal.matches("\\d{3,6}")) {
            return ValidationResult.invalid("Ramal inválido. Informe apenas números (3 a 6 dígitos).");
        }

        return ValidationResult.valid();
    }
}
