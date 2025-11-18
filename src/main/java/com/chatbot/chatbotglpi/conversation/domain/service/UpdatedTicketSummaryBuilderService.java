package com.chatbot.chatbotglpi.conversation.domain.service;

import com.chatbot.chatbotglpi.conversation.application.port.input.UpdateSummaryBuilderPort;
import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import org.springframework.stereotype.Service;

@Service
public class UpdatedTicketSummaryBuilderService implements UpdateSummaryBuilderPort {

    @Override
    public String build(ConversationState state) {

        // Concatenando descrição com local e ramal
        String descriptionWithLocation = String.format("%s (Local: %s, Ramal: %s)",
                state.getData("description"),
                state.getData("locate"),
                state.getData("ramal"));

        return String.format("""
                📝 *Resumo do Chamado Atualizado*
                
                🔹 *Título:* %s
                🔹 *Descrição:* %s
                
                ⚠️ O campo acima foi atualizado!
                
                Para continuar, você pode:
                
                ✅ *Confirmar Chamado*
                   Digite *SIM* para finalizar o chamado.
                
                ❌ *Cancelar Chamado*
                   Digite *NÃO* para cancelar o chamado.
                
                🔙 *Editar algum campo*
                   Digite *voltar <campo>* para corrigir qualquer informação.
                   Exemplos:
                   - `voltar descrição` → alterar a descrição
                   - `voltar local` → alterar o local
                   - `voltar ramal` → alterar o ramal
                """,
                state.getData("title"),
                descriptionWithLocation);
    }
}
