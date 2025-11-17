package com.chatbot.chatbotglpi.conversation.domain.state;

import com.chatbot.chatbotglpi.conversation.application.port.input.SummaryBuilderPort;
import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import com.chatbot.chatbotglpi.conversation.domain.enums.StateEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompletedState implements ChatState{
    private final SummaryBuilderPort summaryBuilderPort;
    private final GreetingState greetingState;
    @Override
    public String handleMessage(ConversationState state, String message) {
        String lowerMsg = message.toLowerCase().trim();
        if (lowerMsg.equals("oi") || lowerMsg.equals("start")) {
            state.setCurrentState(StateEnum.GREETING); // Volta ao estado inicial
            return greetingState.handleMessage(state, message); // <-- chama o handleMessage do greetingState
        }

        return "Conversa já finalizada. Para iniciar novamente, envie uma nova solicitação.";
    }
}
