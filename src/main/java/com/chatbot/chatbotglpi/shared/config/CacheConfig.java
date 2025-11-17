package com.chatbot.chatbotglpi.shared.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuração de Cache L2 (Cache Local em Memória).
 *
 * Esta classe configura o Caffeine como cache de segundo nível (L2).
 * O cache L2 reduz drasticamente a latência ao evitar chamadas de rede ao Redis
 * para dados frequentemente acessados.
 *
 * ARQUITETURA DE CACHE:
 *
 * ┌─────────────┐
 * │  Controller │
 * └──────┬──────┘
 *        │
 *        ▼
 * ┌─────────────────────────────────────┐
 * │    ConversationManager              │
 * │                                     │
 * │  1. Busca no Cache L2 (Caffeine)   │ ◄── MUITO RÁPIDO (nanosegundos)
 * │     ↓ Se não encontrar             │
 * │  2. Busca no Redis (Rede)          │ ◄── RÁPIDO (milissegundos)
 * │     ↓ Se encontrar                 │
 * │  3. Salva no Cache L2              │
 * └─────────────────────────────────────┘
 *
 * BENEFÍCIOS:
 * - Reduz latência de rede em 95%+ para conversas ativas
 * - Diminui carga no Redis
 * - Melhora tempo de resposta para usuários frequentes
 * - Escalabilidade: cada instância tem seu cache local
 *
 * TRADE-OFFS:
 * - Uso de memória RAM (controlado por maxSize)
 * - Cache pode ficar desatualizado entre instâncias (aceitável para sessões)
 * - Complexidade adicional (gerenciada pelo Spring automaticamente)
 */
@Configuration // Marca como classe de configuração do Spring
@EnableCaching // Habilita suporte a caching com anotações @Cacheable, @CacheEvict, etc.
public class CacheConfig {
//Aqui e como eu configuro o caffeine e o cache l2
    /**
     * Nome do cache usado para conversações.
     * Usado nas anotações @Cacheable no código.
     */
    public static final String CONVERSATION_CACHE = "conversations";

    /**
     * Configura o CacheManager do Spring para usar Caffeine.
     *
     * O CacheManager é o gerenciador central de todos os caches da aplicação.
     * Quando você usa @Cacheable("conversations"), o Spring usa este manager.
     *
     * @return CacheManager configurado com Caffeine
     */
    @Bean
    public CacheManager cacheManager() {

        // Cria gerenciador de cache baseado em Caffeine
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(CONVERSATION_CACHE);

        // Configura comportamento do cache com Caffeine
        cacheManager.setCaffeine(caffeineConfig());

        return cacheManager;
    }

    /**
     * Configuração detalhada do Caffeine cache.
     *
     * Caffeine é uma biblioteca de cache em memória de alta performance,
     * sucessora do Google Guava Cache, otimizada para JVM moderna.
     *
     * @return Configuração do Caffeine
     */
    private Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()

                // Tamanho máximo do cache (número de entradas)
                // Com 1000 entradas, se cada conversa ocupa ~2KB, usará ~2MB de RAM
                // Ajuste conforme memória disponível e número de usuários ativos
                .maximumSize(1000)

                // Tempo de expiração após último acesso (Time To Idle - TTI)
                // Se uma conversa não for acessada por 5 minutos, é removida do cache L2
                // Nota: Ainda estará no Redis (TTL de 30min), só sai da memória local
                .expireAfterAccess(5, TimeUnit.MINUTES)

                // Tempo de expiração após criação (Time To Live - TTL)
                // Garante que mesmo conversas ativas sejam revalidadas a cada 10 minutos
                // Previne cache "quente" demais que nunca atualiza
                .expireAfterWrite(10, TimeUnit.MINUTES)

                // Tamanho inicial da tabela hash interna
                // Pré-aloca espaço para 100 entradas, evitando redimensionamentos iniciais
                .initialCapacity(100)

                // Habilita estatísticas do cache (útil para monitoramento)
                // Permite ver hit rate, miss rate, evictions, etc via JMX
                .recordStats()

                // Política de remoção: Window TinyLFU
                // Algoritmo inteligente que mantém itens mais usados recentemente
                // Melhor que LRU puro para padrões de acesso de usuários
                .weakKeys(); // Permite GC de chaves não mais referenciadas
    }

    /**
     * COMO USAR O CACHE L2:
     *
     * No código, use anotações do Spring Cache:
     *
     * @Cacheable(value = "conversations", key = "#phone")
     * public ConversationState getConversation(String phone) {
     *     // Busca no Redis (só chamado se não estiver no cache L2)
     *     return redisTemplate.opsForValue().get("conversation:" + phone);
     * }
     *
     * @CachePut(value = "conversations", key = "#phone")
     * public void saveConversation(String phone, ConversationState state) {
     *     // Salva no Redis E atualiza cache L2
     *     redisTemplate.opsForValue().set("conversation:" + phone, state);
     * }
     *
     * @CacheEvict(value = "conversations", key = "#phone")
     * public void deleteConversation(String phone) {
     *     // Remove do Redis E invalida cache L2
     *     redisTemplate.delete("conversation:" + phone);
     * }
     *
     * IMPORTANTE: O Spring gerencia automaticamente a sincronização entre
     * cache L2 (Caffeine) e fonte de dados (Redis). Você só precisa adicionar
     * as anotações nos métodos corretos.
     */

    /**
     * MONITORAMENTO DO CACHE:
     *
     * Expor métricas do Caffeine via Actuator:
     *
     * 1. Acesse: http://localhost:8080/actuator/metrics/cache.gets?tag=cache:conversations
     * 2. Métricas disponíveis:
     *    - cache.gets (hit + miss)
     *    - cache.puts
     *    - cache.evictions
     *    - cache.size
     *
     * Hit Rate ideal: > 80% (significa que 80% das buscas são atendidas pelo cache L2)
     * Miss Rate: < 20%
     *
     * Se hit rate < 50%, considere:
     * - Aumentar maximumSize
     * - Aumentar expireAfterAccess
     * - Revisar padrões de acesso
     */
}
