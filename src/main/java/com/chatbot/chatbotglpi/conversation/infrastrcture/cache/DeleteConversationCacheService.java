package com.chatbot.chatbotglpi.conversation.infrastrcture.cache;

import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import com.chatbot.chatbotglpi.conversation.domain.exception.ConversationException;
import com.chatbot.chatbotglpi.shared.config.ChatbotProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import static com.chatbot.chatbotglpi.shared.config.CacheConfig.CONVERSATION_CACHE;

@Service
//é semântica: indica que a classe fornece algum serviço / operação de negócio ou infraestrutura, mesmo que seja cache ou persistência.
@RequiredArgsConstructor
@Slf4j
public class DeleteConversationCacheService {
    private final ChatbotProperties chatbotProperties;
    private final RedisTemplate<String, ConversationState> redisTemplate;

    /**
     * Remove conversa do Redis E invalida cache L2.
     * <p>
     * A anotação @CacheEvict garante que:
     * 1. O método é executado (remove do Redis)
     * 2. A entrada é automaticamente removida do cache L2
     * 3. Próxima busca será fresh (não retorna dado desatualizado)
     *
     * @param phone Telefone do usuário
     */
    @CacheEvict(value = CONVERSATION_CACHE, key = "#phone")
    public void deleteConversationState(String phone) {
        try {
            String redisKey = chatbotProperties.getRedis().getKeyPrefix() + phone;
            Boolean deleted = redisTemplate.delete(redisKey);

            log.debug("Estado removido do Redis e cache L2 para: {} (existia: {})", phone, deleted);

        } catch (Exception e) {
            log.error("Erro ao deletar estado da conversa: ", e);
            throw new ConversationException("Erro ao deletar estado da conversa", e);
        }
    }
}
