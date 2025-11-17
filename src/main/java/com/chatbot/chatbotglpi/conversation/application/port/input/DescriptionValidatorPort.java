package com.chatbot.chatbotglpi.conversation.application.port.input;

/**
 * Port para validação de descrição.
 * ISP - interface segregada para validação específica.
 */
public interface DescriptionValidatorPort {

    ValidationResult validate(String description);

    record ValidationResult(boolean isValid, String errorMessage) {
        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult invalid(String message) {
            return new ValidationResult(false, message);
        }
    }
}
