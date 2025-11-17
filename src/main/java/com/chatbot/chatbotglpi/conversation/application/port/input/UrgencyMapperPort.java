package com.chatbot.chatbotglpi.conversation.application.port.input;

import java.util.Optional;

/**
 * Port para mapeamento de urgências.
 * OCP - aberto para extensão de novas urgências.
 */
public interface UrgencyMapperPort {

    Optional<String> map(String input);

    String getAvailableOptions();
}
