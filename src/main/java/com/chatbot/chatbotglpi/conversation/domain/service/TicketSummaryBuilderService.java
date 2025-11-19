package com.chatbot.chatbotglpi.conversation.domain.service;

import com.chatbot.chatbotglpi.conversation.application.port.input.SummaryBuilderPort;
import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import org.springframework.stereotype.Service;

/**
 * Domain Service para construção de resumos de tickets.
 * Contém lógica de apresentação do domínio.
 */
@Service
public class TicketSummaryBuilderService implements SummaryBuilderPort {

    @Override
    public String build(ConversationState state) {
        return String.format("""
                        📋 *RESUMO DO CHAMADO*
                        
                        👤 *Usuário:*
                        %s
                        
                        📝 *Descrição:*
                        %s
                        
                        📍 *Local:*
                        %s
                        
                        ☎️ *Ramal:*
                        %s
                        
                        ✔️ Se estiver tudo correto, basta confirmar para abrir o chamado.
                        """,
                state.getData("username"),
                state.getData("description"),
                state.getData("locate"),
                state.getData("ramal")
        );
    }
}
