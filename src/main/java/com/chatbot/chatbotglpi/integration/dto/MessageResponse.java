package com.chatbot.chatbotglpi.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private List<String> messages;

    public MessageResponse(String... messages) {
        this.messages = Arrays.asList(messages);
    }

}
