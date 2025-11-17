package com.chatbot.chatbotglpi.conversation.application.port.input;

import java.util.Optional;

/**
 * Port para mapeamento de categorias.
 * OCP - aberto para extensão de novas categorias.
 * Define o que o sistema pode fazer.
 */
public interface CategoryMapperPort {

    Optional<String> map(String input);

    String getAvailableOptions();
}
