package com.chatbot.chatbotglpi.integration.glpi;

import com.chatbot.chatbotglpi.integration.glpi.dto.CreateTicketRequest;

// Pode ser uma classe interna ou um novo arquivo .java

public record GlpiTicketPayload(CreateTicketRequest input) {
}

