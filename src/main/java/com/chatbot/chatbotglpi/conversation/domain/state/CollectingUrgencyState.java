package com.chatbot.chatbotglpi.conversation.domain.state;

import com.chatbot.chatbotglpi.conversation.application.port.input.SummaryBuilderPort;
import com.chatbot.chatbotglpi.conversation.application.port.input.UrgencyMapperPort;
import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import com.chatbot.chatbotglpi.conversation.domain.enums.StateEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Estado de coleta de urgência.
 * SRP - única responsabilidade de coletar urgência.
 * DIP - depende de abstrações (ports).
 */
@Component
@RequiredArgsConstructor
public class CollectingUrgencyState implements ChatState {

    private final UrgencyMapperPort urgencyMapper;
    private final SummaryBuilderPort summaryBuilder;

    @Override
    public String handleMessage(ConversationState state, String message) {
        // 1. Mapeia urgência (DIP - via abstração)
        String urgency = urgencyMapper.map(message.trim())
                .orElse(null);

        // 2. Valida
        if (urgency == null) {
            return "Opção inválida. Digite um número de 1 a 4.";
        }

        // 3. Salva urgência
        state.addData("urgency", urgency);

        // 4. Avança para próximo estado
        state.setCurrentState(StateEnum.CONFIRMING);

        // 5. Verifica se dados estão completos
        if (!state.isComplete()) {
            return "Erro interno: dados incompletos. Digite /cancelar e comece novamente.";
        }

        // 6. Retorna resumo para confirmação (DIP - via abstração)
        return summaryBuilder.build(state);
    }
}
