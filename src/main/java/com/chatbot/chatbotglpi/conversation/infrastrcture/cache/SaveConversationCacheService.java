package com.chatbot.chatbotglpi.conversation.infrastrcture.cache;

import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import com.chatbot.chatbotglpi.conversation.domain.exception.ConversationException;
import com.chatbot.chatbotglpi.shared.config.ChatbotProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

import static com.chatbot.chatbotglpi.shared.config.CacheConfig.CONVERSATION_CACHE;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaveConversationCacheService {
    private final ChatbotProperties chatbotProperties;
    private final RedisTemplate<String, ConversationState> redisTemplate;
    /**
     * Salva estado da conversa no Redis E atualiza cache L2.
     * <p>
     * A anotação @CachePut garante que:
     * 1. O método SEMPRE é executado (salva no Redis)
     * 2. O resultado é automaticamente salvo no cache L2
     * 3. Cache L2 fica sincronizado com Redis
     *
     * @param phone Telefone do usuário
     * @param state Estado da conversa a salvar
     */
    @CachePut(value = CONVERSATION_CACHE, key = "#phone")
    public ConversationState saveConversationState(String phone, ConversationState state) {
        try {
            String redisKey = chatbotProperties.getRedis().getKeyPrefix() + phone;

            // Obtém TTL das configurações
            int ttlMinutes = chatbotProperties.getConversation().getTtlMinutes();

            // Salva no Redis com expiração automática
            redisTemplate.opsForValue().set(
                    redisKey,
                    state,
                    Duration.ofMinutes(ttlMinutes)
            );

            log.debug("Estado salvo no Redis e cache L2 para: {} (TTL: {}min)", phone, ttlMinutes);

            return state; // @CachePut cacheia o retorno

        } catch (Exception e) {
            log.error("Erro ao salvar estado da conversa: ", e);
            throw new ConversationException("Erro ao salvar estado da conversa", e);
        }
    }


}
