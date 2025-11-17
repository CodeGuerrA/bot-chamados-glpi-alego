package com.chatbot.chatbotglpi.conversation.domain.service;

import com.chatbot.chatbotglpi.conversation.application.port.input.LocateValidatorPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocateValidatorService implements LocateValidatorPort {

    @Override
    public ValidationResult validate(String locate) {
        if (locate == null || locate.isBlank()) {
            return ValidationResult.invalid("Local não pode ser vazio ");
        }

        return ValidationResult.valid();
    }
}
