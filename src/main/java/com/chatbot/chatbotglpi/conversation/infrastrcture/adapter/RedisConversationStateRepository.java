package com.chatbot.chatbotglpi.conversation.infrastrcture.adapter;

import com.chatbot.chatbotglpi.conversation.application.port.output.ConversationStateRepository;
import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import com.chatbot.chatbotglpi.conversation.infrastrcture.cache.DeleteConversationCacheService;
import com.chatbot.chatbotglpi.conversation.infrastrcture.cache.GetConversationStateCacheService;
import com.chatbot.chatbotglpi.conversation.infrastrcture.cache.SaveConversationCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Adapter que implementa o repositório de estado usando Redis.
 * DIP - implementa a abstração definida na camada de aplicação.
 * ISP - implementa apenas os métodos necessários.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisConversationStateRepository implements ConversationStateRepository {

    private final GetConversationStateCacheService getConversationStateCacheService;
    private final SaveConversationCacheService saveConversationCacheService;
    private final DeleteConversationCacheService deleteConversationCacheService;

    @Override
    public Optional<ConversationState> findByPhone(String phone) {
        log.debug("Buscando estado da conversa para {}", phone);
        ConversationState state = getConversationStateCacheService.getConversationState(phone);
        return Optional.ofNullable(state);
    }

    @Override
    public ConversationState save(String phone, ConversationState state) {
        log.debug("Salvando estado da conversa para {}", phone);
        return saveConversationCacheService.saveConversationState(phone, state);
    }

    @Override
    public void delete(String phone) {
        log.debug("Deletando estado da conversa para {}", phone);
        deleteConversationCacheService.deleteConversationState(phone);
    }
}
