package com.chatbot.chatbotglpi.conversation.application.port.input;

import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;

/**
 * Port para construção de resumos.
 * SRP - única responsabilidade de construir resumos.
 */
public interface SummaryBuilderPort {

    String build(ConversationState state);
}
