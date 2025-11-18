package com.chatbot.chatbotglpi.conversation.application.port.input;

public interface UsernameVlidatorPort {
    ValidationResult validate(String username);

    record ValidationResult(boolean isValid, String errorMessage) {
        public static UsernameVlidatorPort.ValidationResult valid() {
            return new UsernameVlidatorPort.ValidationResult(true, null);
        }

        public static UsernameVlidatorPort.ValidationResult invalid(String message) {
            return new UsernameVlidatorPort.ValidationResult(false, message);
        }
    }
}
