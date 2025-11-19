package com.chatbot.chatbotglpi.conversation.domain.validator.locate;

import com.chatbot.chatbotglpi.conversation.domain.validator.base.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocateValidator implements Validator<String> {

    @Override
    public ValidationResult validate(String locate) {
        if (locate == null || locate.isBlank()) {
            return ValidationResult.invalid("Local não pode ser vazio ");
        }

        return ValidationResult.valid();
    }
}
