package com.chatbot.chatbotglpi.conversation.domain.service;

import com.chatbot.chatbotglpi.conversation.application.port.input.UpdateSummaryBuilderPort;
import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import org.springframework.stereotype.Service;

@Service
public class UpdatedTicketSummaryBuilderService implements UpdateSummaryBuilderPort {

    @Override
    public String build(ConversationState state) {
        return String.format("""
                        📋 *DADOS ATUALIZADOS DO CHAMADO*
                        
                        👤 *Usuário:*
                        %s
                        
                        📝 *Descrição:*
                        %s
                        
                        📍 *Local:*
                        %s
                        
                        ☎️ *Ramal:*
                        %s
                        
                        ✔️ Para continuar, digite *SIM* para confirmar.
                        ❌ Para cancelar, digite *NÃO*.
                        ✏️ Para ajustar alguma informação, responda com *1*, *2*, *3* ou *4*.
                        """,
                state.getData("username"),
                state.getData("description"),
                state.getData("locate"),
                state.getData("ramal")
        );
    }
}
