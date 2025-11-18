package com.chatbot.chatbotglpi.integration.glpi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GlpiSessionResponse {
    @JsonProperty("session_token")
    private String sessionToken;
}
