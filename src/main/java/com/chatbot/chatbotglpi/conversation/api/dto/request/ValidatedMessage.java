package com.chatbot.chatbotglpi.conversation.api.dto.request;

import lombok.Builder;
import lombok.Getter;

/**
 * DTO imutável representando uma mensagem validada e sanitizada.
 */
@Getter
@Builder
public class ValidatedMessage {

    private final String phone;
    private final String message;
    private final boolean valid;
    private final String errorMessage;

    public static ValidatedMessage valid(String phone, String message) {
        return ValidatedMessage.builder()
                .phone(phone)
                .message(message)
                .valid(true)
                .build();
    }

    public static ValidatedMessage invalid(String errorMessage) {
        return ValidatedMessage.builder()
                .valid(false)
                .errorMessage(errorMessage)
                .build();
    }
}
