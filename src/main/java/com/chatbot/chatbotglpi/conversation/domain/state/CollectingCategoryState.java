package com.chatbot.chatbotglpi.conversation.domain.state;

import com.chatbot.chatbotglpi.conversation.application.port.input.CategoryMapperPort;
import com.chatbot.chatbotglpi.conversation.application.port.input.UrgencyMapperPort;
import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import com.chatbot.chatbotglpi.conversation.domain.enums.StateEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Estado de coleta de categoria.
 * SRP - única responsabilidade de coletar categoria.
 * DIP - depende de abstrações (ports).
 */
@Component
@RequiredArgsConstructor
public class CollectingCategoryState implements ChatState {

    private final CategoryMapperPort categoryMapper;
    private final UrgencyMapperPort urgencyMapper;

    @Override
    public String handleMessage(ConversationState state, String message) {
        // 1. Mapeia categoria (DIP - via abstração)
        String category = categoryMapper.map(message.trim())
                .orElse(null);

        // 2. Valida
        if (category == null) {
            return "Opção inválida. Digite um número de 1 a 4.";
        }

        // 3. Salva categoria
        state.addData("category", category);

        // 4. Avança para próximo estado
        state.setCurrentState(StateEnum.COLLECTING_URGENCY);

        // 5. Monta resposta
        String currentUrgency = state.getData("urgency");
        String urgencyOptions = urgencyMapper.getAvailableOptions();

        return "Categoria registrada!\n" +
                "Qual a *urgência* deste problema?\n" +
                urgencyOptions +
                (currentUrgency != null ? "\nUrgência atual: " + currentUrgency : "\nDigite o número:");
    }
}
