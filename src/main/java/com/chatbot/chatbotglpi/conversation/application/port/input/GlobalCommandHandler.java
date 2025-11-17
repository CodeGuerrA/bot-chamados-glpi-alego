package com.chatbot.chatbotglpi.conversation.application.port.input;

import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;

import java.util.Optional;

/**
 * Port para processamento de comandos globais.
 * OCP - aberto para extensão (novos comandos), fechado para modificação.
 */
public interface GlobalCommandHandler {

    /**
     * Processa um comando global se aplicável.
     * @return Optional com resposta se o comando foi processado, empty caso contrário
     */
    Optional<String> handle(String message, ConversationState state);

    /**
     * Verifica se a mensagem é um comando global.
     */
    boolean isGlobalCommand(String message);
}
