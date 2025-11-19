package com.chatbot.chatbotglpi.conversation.infrastrcture.adapter;

import com.chatbot.chatbotglpi.conversation.application.port.output.ConversationStateRepository;
import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import com.chatbot.chatbotglpi.conversation.infrastrcture.cache.DeleteConversationCacheService;
import com.chatbot.chatbotglpi.conversation.infrastrcture.cache.GetConversationStateCacheService;
import com.chatbot.chatbotglpi.conversation.infrastrcture.cache.SaveConversationCacheService;
import com.chatbot.chatbotglpi.shared.config.ChatbotProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    private final RedisTemplate<String, ConversationState> redisTemplate;
    private final ChatbotProperties chatbotProperties;

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

    @Override
    public List<ConversationState> findAll() {
        log.debug("Buscando todas as conversas ativas no Redis");
        try {
            String keyPattern = chatbotProperties.getRedis().getKeyPrefix() + "*";
            Set<String> keys = redisTemplate.keys(keyPattern);

            if (keys == null || keys.isEmpty()) {
                log.debug("Nenhuma conversa ativa encontrada");
                return List.of();
            }

            List<ConversationState> conversations = new ArrayList<>();
            for (String key : keys) {
                ConversationState state = redisTemplate.opsForValue().get(key);
                if (state != null) {
                    conversations.add(state);
                }
            }

            log.debug("Total de conversas ativas: {}", conversations.size());
            return conversations;

        } catch (Exception e) {
            log.error("Erro ao buscar todas as conversas", e);
            return List.of();
        }
    }
}
