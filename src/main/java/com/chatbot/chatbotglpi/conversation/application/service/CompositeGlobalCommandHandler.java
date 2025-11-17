package com.chatbot.chatbotglpi.conversation.application.service;

import com.chatbot.chatbotglpi.conversation.application.port.input.GlobalCommandHandler;
import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Handler composto que delega para múltiplos handlers de comandos.
 * OCP - aberto para extensão (novos handlers), fechado para modificação.
 * Composite Pattern - combina múltiplos handlers.
 *
 * Para adicionar um novo comando global:
 * 1. Crie uma nova classe implementando GlobalCommandHandler
 * 2. O Spring automaticamente a registrará aqui
 */
@Slf4j
@Component
@Primary
public class CompositeGlobalCommandHandler implements GlobalCommandHandler {

    private final List<GlobalCommandHandler> handlers;

    /**
     * Spring injeta automaticamente todos os GlobalCommandHandler,
     * exceto este próprio (evita recursão infinita).
     */
    public CompositeGlobalCommandHandler(List<GlobalCommandHandler> handlers) {
        // Filtra para não incluir a si mesmo
        this.handlers = handlers.stream()
                .filter(h -> !(h instanceof CompositeGlobalCommandHandler))
                .toList();

        log.info("CompositeGlobalCommandHandler inicializado com {} handlers", this.handlers.size());
        this.handlers.forEach(h ->
                log.debug("Handler registrado: {}", h.getClass().getSimpleName())
        );
    }

    @Override
    public Optional<String> handle(String message, ConversationState state) {
        // Delega para o primeiro handler que processar o comando
        for (GlobalCommandHandler handler : handlers) {
            Optional<String> result = handler.handle(message, state);
            if (result.isPresent()) {
                log.debug("Comando processado por: {}", handler.getClass().getSimpleName());
                return result;
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean isGlobalCommand(String message) {
        // Verifica se algum handler reconhece o comando
        return handlers.stream()
                .anyMatch(h -> h.isGlobalCommand(message));
    }
}
