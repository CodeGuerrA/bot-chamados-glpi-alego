package com.chatbot.chatbotglpi.conversation.application.port.output;

import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;

import java.util.Optional;

/**
 * Port de saída para persistência do estado da conversa.
 * Seguindo DIP - depende de abstração, não de implementação concreta.
 * Define o que o caso de uso precisa que o mundo externo faça por ele.
 */
public interface ConversationStateRepository {

    Optional<ConversationState> findByPhone(String phone);

    ConversationState save(String phone, ConversationState state);

    void delete(String phone);
}
