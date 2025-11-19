package com.chatbot.chatbotglpi.conversation.domain.validator.base;

public interface Validator<T> {
    ValidationResult validate(T value);

    record ValidationResult(boolean isValid, String errorMessage) {
        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult invalid(String message) {
            return new ValidationResult(false, message);
        }
    }
}
