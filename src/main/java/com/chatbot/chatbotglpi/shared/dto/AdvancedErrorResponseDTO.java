package com.chatbot.chatbotglpi.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdvancedErrorResponseDTO {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private List<String> userMessages;
    private String path;
    private DebugInfo debugInfo;

    public AdvancedErrorResponseDTO(LocalDateTime timestamp, int status, String error,
                                    List<String> userMessages, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.userMessages = userMessages;
        this.path = path;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL) //isso server para nao incluir valores null na saida JSON
    public static class DebugInfo {
        private String exceptionClass;
        private String exceptionMessage;
        private List<String> stackTrace;
    }
}