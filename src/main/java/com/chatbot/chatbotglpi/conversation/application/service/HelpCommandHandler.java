package com.chatbot.chatbotglpi.conversation.application.service;

import com.chatbot.chatbotglpi.conversation.application.port.input.GlobalCommandHandler;
import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

/**
 * Handler para o comando "ajuda".
 * OCP - pode ser adicionado sem modificar código existente.
 * SRP - única responsabilidade: mostrar ajuda.
 */
@Slf4j
@Component
public class HelpCommandHandler implements GlobalCommandHandler {

    private static final Set<String> HELP_COMMANDS = Set.of("ajuda", "help", "?");

    @Override
    public Optional<String> handle(String message, ConversationState state) {
        if (!isGlobalCommand(message)) {
            return Optional.empty();
        }

        log.info("Comando 'ajuda' processado para {}", state.getPhone());

        String helpMessage = """
                *Comandos disponíveis:*

                - *voltar* - Volta para a etapa anterior
                - *ajuda* ou *?* - Mostra esta mensagem
                - *cancelar* - Cancela a conversa atual

                *Dicas:*
                - Digite *oi* para iniciar um novo chamado
                - Na confirmação, use *voltar <campo>* para editar (ex: voltar descrição)
                - Digite *SIM* para confirmar ou *NÃO* para cancelar
                """;

        return Optional.of(helpMessage);
    }

    @Override
    public boolean isGlobalCommand(String message) {
        return message != null && HELP_COMMANDS.contains(message.trim().toLowerCase());
    }
}
