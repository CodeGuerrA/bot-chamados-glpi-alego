package com.chatbot.chatbotglpi.conversation.application.service;

import com.chatbot.chatbotglpi.conversation.application.port.input.GlobalCommandHandler;
import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import com.chatbot.chatbotglpi.conversation.domain.enums.StateEnum;
import com.chatbot.chatbotglpi.conversation.domain.helper.StateNavigationHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Handler para o comando "voltar".
 * OCP - pode ser estendido para novos comandos sem modificar código existente.
 * SRP - única responsabilidade: processar comando de voltar.
 */
@Slf4j
@Component
public class BackCommandHandler implements GlobalCommandHandler {

    private static final String BACK_COMMAND = "voltar";

    @Override
    public Optional<String> handle(String message, ConversationState state) {
        if (message == null || message.trim().isEmpty()) {
            return Optional.empty();
        }

        String cleanedMessage = message.trim();

        // Checa digitação aproximada (até 2 erros)
        LevenshteinDistance distance = new LevenshteinDistance();
        int diff = distance.apply(cleanedMessage.toLowerCase(), BACK_COMMAND);

        if (diff <= 2 && !cleanedMessage.equalsIgnoreCase(BACK_COMMAND)) {
            return Optional.of("Parece que você tentou digitar 'voltar'. Por favor, digite *voltar* corretamente.");
        }

        // Se não é o comando "voltar" exato, ignora
        if (!cleanedMessage.equalsIgnoreCase(BACK_COMMAND)) {
            return Optional.empty();
        }

        // Processa comando correto
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
        if (message == null || message.trim().isEmpty()) return false;

        String cleaned = message.trim();

        // Comando exato
        if (cleaned.equalsIgnoreCase(BACK_COMMAND)) return true;

        // Digitação aproximada (até 2 erros) Com o Levenshtein, qualquer mensagem que tenha até 2 diferenças em relação a "voltar" é reconhecida como tentativa do comando.
        LevenshteinDistance distance = new LevenshteinDistance();
        return distance.apply(cleaned.toLowerCase(), BACK_COMMAND) <= 2;
    }
}
