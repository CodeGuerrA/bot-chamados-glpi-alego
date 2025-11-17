package com.chatbot.chatbotglpi.conversation.application.port.input;

import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;

/**
 * Port para processamento de estados da conversa.
 * ISP - interface segregada para processamento específico de estados.
 */
public interface StateProcessorPort {

    String process(ConversationState state, String message);
}
