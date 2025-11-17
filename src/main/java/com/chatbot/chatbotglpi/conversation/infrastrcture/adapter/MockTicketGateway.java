package com.chatbot.chatbotglpi.conversation.infrastrcture.adapter;

import com.chatbot.chatbotglpi.conversation.application.port.output.TicketGateway;
import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Adapter mock para criação de tickets.
 * Pode ser substituído por GLPITicketGateway futuramente.
 * DIP - implementa a abstração definida na camada de aplicação.
 */
@Slf4j
@Component
public class MockTicketGateway implements TicketGateway {

    @Override
    public Long createTicket(ConversationState state) {
        Long ticketId = System.currentTimeMillis() % 100000;

        log.info("Ticket criado (MOCK): {} para usuário {} - Título: {}, Local: {}, Ramal: {}",
                ticketId,
                state.getPhone(),
                state.getData("title"),
                state.getData("locate"),
                state.getData("ramal"));
//                state.getData("category"),
//                state.getData("urgency"));

        return ticketId;
    }
}
