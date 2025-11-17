package com.chatbot.chatbotglpi.integration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SendMessageRequest {

    @JsonProperty("number")
    private String number;        // Número sem @s.whatsapp.net

    @JsonProperty("text")
    private String text;          // Texto da mensagem

    @JsonProperty("delay")
    private Integer delay;
}
