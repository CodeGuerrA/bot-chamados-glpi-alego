package com.chatbot.chatbotglpi.conversation.domain.service;

import com.chatbot.chatbotglpi.conversation.application.port.input.UsernameVlidatorPort;

public class UsernameValidatorService implements UsernameVlidatorPort {

    private static final String USERNAME_REGEX = "^[a-zA-ZÀ-ÿ]+\\.[a-zA-ZÀ-ÿ]+(\\d*)?$";

    @Override
    public ValidationResult validate(String username) {

        if (username == null || username.isBlank()) {
            return ValidationResult.invalid("Username não pode ser vazio");
        }

        if (!username.matches(USERNAME_REGEX)) {
            return ValidationResult.invalid("Username inválido. Use o formato nome.sobrenome");
        }

        return ValidationResult.valid();
    }
}
