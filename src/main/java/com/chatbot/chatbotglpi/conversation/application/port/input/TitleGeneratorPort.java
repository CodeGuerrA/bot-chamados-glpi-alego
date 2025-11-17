package com.chatbot.chatbotglpi.conversation.application.port.input;

/**
 * Port para geração de títulos.
 * ISP - interface segregada para geração de títulos.
 */
public interface TitleGeneratorPort {

    String generateTitle(String description);
}
