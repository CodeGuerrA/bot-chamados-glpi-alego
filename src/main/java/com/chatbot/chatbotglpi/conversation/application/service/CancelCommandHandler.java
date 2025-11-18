package com.chatbot.chatbotglpi.conversation.application.service;

import com.chatbot.chatbotglpi.conversation.application.port.input.GlobalCommandHandler;
import com.chatbot.chatbotglpi.conversation.application.port.output.ConversationStateRepository;
import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import com.chatbot.chatbotglpi.conversation.domain.enums.StateEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

/**
 * Handler para o comando "cancelar".
 * OCP - pode ser adicionado sem modificar código existente.
 * SRP - única responsabilidade: cancelar conversa.
 */
@Slf4j
@Component
@RequiredArgsConstructor
//arrumar isso daqui dps sobre isso de cancelar.
public class CancelCommandHandler implements GlobalCommandHandler {

    private static final Set<String> CANCEL_COMMANDS = Set.of("cancelar", "/cancelar", "cancel", "NÃO", "nao", "não");

    private final ConversationStateRepository conversationRepository;

    @Override
    public Optional<String> handle(String message, ConversationState state) {
        if (!isGlobalCommand(message)) {
            return Optional.empty();
        }

        log.info("Comando 'cancelar' processado para {}", state.getPhone());

        // Deleta conversa do repositório
        conversationRepository.delete(state.getPhone());

        // Marca como completo para não persistir
        state.setCurrentState(StateEnum.COMPLETED);

        return Optional.of("""
                Conversa cancelada.

                Digite *oi* para começar novamente.
                """);
    }

    @Override
    public boolean isGlobalCommand(String message) {
        return message != null && CANCEL_COMMANDS.contains(message.trim().toLowerCase());
    }
}
