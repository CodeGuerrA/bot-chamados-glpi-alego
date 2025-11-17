package com.chatbot.chatbotglpi.conversation.infrastrcture.cache;

import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import com.chatbot.chatbotglpi.shared.config.ChatbotProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import static com.chatbot.chatbotglpi.shared.config.CacheConfig.CONVERSATION_CACHE;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetConversationStateCacheService {
private final ChatbotProperties chatbotProperties;
    private final RedisTemplate<String, ConversationState> redisTemplate;
    /**
     * Recupera estado da conversa com cache L2.
     * <p>
     * FLUXO DE BUSCA:
     * 1. Verifica cache L2 (Caffeine) - ~1ms
     * 2. Se não encontrar (MISS), busca no Redis - ~10-50ms
     * 3. Se encontrar no Redis, salva no cache L2 para próxima vez
     * 4. Retorna o estado ou null
     * <p>
     * A anotação @Cacheable faz toda a mágica:
     * - key="#phone" -> usa telefone como chave do cache
     * - unless="#result == null" -> não cacheia valores null
     *
     * @param phone Telefone do usuário
     * @return ConversationState ou null se não existir
     */
    @Cacheable(value = CONVERSATION_CACHE, key = "#phone", unless = "#result == null")
    public ConversationState getConversationState(String phone) {
        try {
            String redisKey = chatbotProperties.getRedis().getKeyPrefix() + phone;
            ConversationState state = redisTemplate.opsForValue().get(redisKey);

            if (state != null) {
                log.debug("Estado recuperado do Redis para: {}", phone);
            }

            return state;

        } catch (Exception e) {
            log.error("Erro ao recuperar estado da conversa do Redis: ", e);
            return null; // Retorna null em caso de erro (não cacheia)
        }
    }
}
