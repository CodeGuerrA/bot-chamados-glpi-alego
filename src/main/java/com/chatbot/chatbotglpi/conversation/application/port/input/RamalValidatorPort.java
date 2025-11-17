package com.chatbot.chatbotglpi.conversation.application.port.input;

public interface RamalValidatorPort {
    ValidationResult validate(String ramal);


    record ValidationResult(boolean isValid, String errorMessage) {
        public static RamalValidatorPort.ValidationResult valid() {
            return new RamalValidatorPort.ValidationResult(true, null);
        }

        public static RamalValidatorPort.ValidationResult invalid(String message) {
            return new RamalValidatorPort.ValidationResult(false, message);
        }
    }
}
