package com.chatbot.chatbotglpi.conversation.domain.state;

import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import com.chatbot.chatbotglpi.conversation.domain.enums.StateEnum;
import org.springframework.stereotype.Component;

/**
 * Estado de saudação inicial.
 * SRP - única responsabilidade de cumprimentar e iniciar coleta.
 */
@Component
public class GreetingState implements ChatState {

    @Override
    public String handleMessage(ConversationState state, String message) {
        state.setCurrentState(StateEnum.COLLECTING_DESCRIPTION);

        return """
                Olá aqui e o Bot do Suporte!
                Vou te ajudar a abrir um novo chamado no sistema.

                Por favor, descreva o problema que está enfrentando:
                """;
    }
}
