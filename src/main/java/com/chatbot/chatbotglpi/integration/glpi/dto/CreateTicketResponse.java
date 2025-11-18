package com.chatbot.chatbotglpi.integration.glpi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateTicketResponse {
    private Integer id; //id do ticket
    private String message; //mensagem de sucesso de criação de ticket.

}
