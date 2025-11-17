package com.chatbot.chatbotglpi.conversation.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageResponse {

    private String response;
    private String currentState;
    private LocalDateTime timestamp;
    private Boolean completed;

    public MessageResponse(String response) {
        this.response = response;
        this.timestamp = LocalDateTime.now();
    }

    public static MessageResponse of(String response) {
        return new MessageResponse(response);
    }

    public static MessageResponse of(String response, String currentState, Boolean completed) {
        return MessageResponse.builder()
                .response(response)
                .currentState(currentState)
                .completed(completed)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
