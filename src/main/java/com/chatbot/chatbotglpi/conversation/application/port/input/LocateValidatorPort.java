package com.chatbot.chatbotglpi.conversation.application.port.input;

public interface LocateValidatorPort {

    ValidationResult validate(String locate);

    record ValidationResult(boolean isValid, String errorMessage) {
        public static LocateValidatorPort.ValidationResult valid() {
            return new LocateValidatorPort.ValidationResult(true, null);
        }

        public static LocateValidatorPort.ValidationResult invalid(String message) {
            return new LocateValidatorPort.ValidationResult(false, message);
        }
    }
}
