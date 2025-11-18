package com.chatbot.chatbotglpi.integration.glpi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateTicketRequest {

    @JsonProperty("name")
    private String name;

    @JsonProperty("content")
    private String content;

    @JsonProperty("type")
    private Integer type;       // 1 = Incidente, 2 = Requisição

    @JsonProperty("status")
    private Integer status;     // 1 = Novo, 2 = Processando, etc.

    @JsonProperty("_users_id_requester")
    private Integer requesterId; // opcional: ID do usuário solicitante
}
