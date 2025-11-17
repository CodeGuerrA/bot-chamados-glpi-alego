package com.chatbot.chatbotglpi.conversation.application.service;

import com.chatbot.chatbotglpi.conversation.application.port.input.GlobalCommandHandler;
import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import com.chatbot.chatbotglpi.conversation.domain.enums.StateEnum;
import com.chatbot.chatbotglpi.conversation.domain.helper.StateNavigationHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Handler para o comando "voltar".
 * OCP - pode ser estendido para novos comandos sem modificar código existente.
 * SRP - única responsabilidade: processar comando de voltar.é um componente responsável por processar um tipo específico de evento, comando, requisição ou mensagem dentro de um sistema.
 */
@Slf4j
@Component
public class BackCommandHandler implements GlobalCommandHandler {

    private static final String BACK_COMMAND = "voltar";

    @Override
    public Optional<String> handle(String message, ConversationState state) {
        if (!isGlobalCommand(message)) {
            return Optional.empty();
        }

        log.info("Processando comando 'voltar' para {}", state.getPhone());

        StateEnum currentState = state.getCurrentState();
        StateEnum previousState = StateNavigationHelper.getPrevious(currentState);

        if (previousState == currentState) {
            log.debug("Não é possível voltar do estado {}", currentState);
            return Optional.of("Não é possível voltar desta etapa.");
        }

        state.setCurrentState(previousState);

        log.info("Estado alterado de {} para {}", currentState, previousState);

        return Optional.of("Você voltou para a etapa anterior. Por favor, informe novamente.");
    }

    @Override
    public boolean isGlobalCommand(String message) {
        return message != null && message.trim().equalsIgnoreCase(BACK_COMMAND);
    }
}
