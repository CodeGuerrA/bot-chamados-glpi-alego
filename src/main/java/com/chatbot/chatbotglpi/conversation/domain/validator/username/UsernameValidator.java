package com.chatbot.chatbotglpi.conversation.domain.validator.username;

import com.chatbot.chatbotglpi.conversation.domain.validator.base.Validator;
import org.springframework.stereotype.Service;

@Service
public class UsernameValidator implements Validator<String> {

    private static final String USERNAME_REGEX = "^[a-zA-ZÀ-ÿ]+\\.[a-zA-ZÀ-ÿ]+(\\d*)?$";

    @Override
    public ValidationResult validate(String username) {

        if (username == null || username.isBlank()) {
            return ValidationResult.invalid("Username não pode ser vazio");
        }

        if (!username.matches(USERNAME_REGEX)) {
            return ValidationResult.invalid("Formato de username inválido.");
        }

        return ValidationResult.valid();
    }
}
