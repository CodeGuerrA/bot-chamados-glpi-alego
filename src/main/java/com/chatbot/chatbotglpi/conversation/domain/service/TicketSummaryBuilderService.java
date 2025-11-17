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
                        📋 *Resumo do Chamado*
                        
                        📌 Título: %s
                        ✍️ Descrição: %s
                        📍 Local: %s
                        📞 Ramal: %s
                        
                        ✅ Digite *SIM* para confirmar
                        ❌ Digite *NÃO* para cancelar
                        🔙 Digite *voltar <campo>* para editar (ex: voltar descrição)
                        """,
                state.getData("title"),
                state.getData("description"),
//                state.getData("category"),
//                state.getData("urgency"));
                state.getData("locate"),
                state.getData("ramal"));
    }
}
