package com.chatbot.chatbotglpi.conversation.application.port.output;

import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;

/**
 * Port de saída para criação de tickets.
 * DIP - abstração para sistema externo (GLPI, mock, etc).
 */
public interface TicketGateway {

    /**
     * Cria um ticket no sistema externo.
     * @return ID do ticket criado
     */
    Long createTicket(ConversationState state);
}
