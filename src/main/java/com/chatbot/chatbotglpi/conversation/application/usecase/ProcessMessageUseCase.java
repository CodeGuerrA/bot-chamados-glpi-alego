package com.chatbot.chatbotglpi.conversation.application.usecase;

import com.chatbot.chatbotglpi.conversation.application.port.input.StateProcessorPort;
import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import com.chatbot.chatbotglpi.conversation.domain.state.ChatState;
import com.chatbot.chatbotglpi.conversation.domain.state.StateFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Use Case para processamento de mensagens.
 * Operação única: delega processamento ao estado correto.
 * SRP - Single Responsibility Principle.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessMessageUseCase implements StateProcessorPort {

    private final StateFactory stateFactory;

    @Override
    public String process(ConversationState state, String message) {
        log.debug("Processando mensagem no estado {}", state.getCurrentState());

        ChatState currentStateHandler = stateFactory.getState(state.getCurrentState());
        return currentStateHandler.handleMessage(state, message);
    }
}
