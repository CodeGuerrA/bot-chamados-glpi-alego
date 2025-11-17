package com.chatbot.chatbotglpi.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SendMessageResponse {

    private Key key; // agora é objeto
    private String status;
    private Object message;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Key {
        private String remoteJid;
        private String id;
    }
}
