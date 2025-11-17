package com.chatbot.chatbotglpi.conversation.domain.service;

import com.chatbot.chatbotglpi.conversation.application.port.input.UpdateSummaryBuilderPort;
import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import org.springframework.stereotype.Service;

@Service
public class UpdatedTicketSummaryBuilderService implements UpdateSummaryBuilderPort {

    @Override
    public String build(ConversationState state) {
        return String.format("""
                📝 *Resumo do Chamado Atualizado*

                🔹 *Título:* %s
                🔹 *Descrição:* %s
                🔹 *Local:* %s
                🔹 *Ramal:* %s

                ⚠️ *Campo atualizado!*
                Deseja atualizar mais algum valor? Caso não, escolha uma das opções abaixo:

                ✅ *Confirmar Chamado*
                   Digite *SIM* para finalizar o chamado.

                ❌ *Cancelar Chamado*
                   Digite *NÃO* para cancelar o chamado.

                🔙 *Editar algum campo*
                   Digite *voltar <campo>* para editar um campo específico.
                   Exemplos:
                   - `voltar descrição` → alterar a descrição
                   - `voltar local` → alterar o local
                   - `voltar ramal` → alterar o ramal
                """,
                state.getData("title"),
                state.getData("description"),
                state.getData("locate"),
                state.getData("ramal"));
    }
}
