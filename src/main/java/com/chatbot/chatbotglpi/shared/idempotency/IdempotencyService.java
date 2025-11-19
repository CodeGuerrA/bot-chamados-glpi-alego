package com.chatbot.chatbotglpi.shared.idempotency;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Serviço de idempotência para prevenir processamento duplicado.
 *
 * Usa Redis para rastrear IDs já processados com TTL automático.
 *
 * Casos de uso:
 * - Prevenir tickets duplicados quando webhook é reenviado
 * - Evitar processar a mesma mensagem do WhatsApp múltiplas vezes
 * - Garantir exactly-once semantics
 *
 * Exemplo:
 * <pre>
 * if (idempotencyService.isAlreadyProcessed("message-123")) {
 *     return; // Já processado, ignora
 * }
 * // Processa mensagem...
 * idempotencyService.markAsProcessed("message-123");
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "idempotency:";
    private static final Duration DEFAULT_TTL = Duration.ofHours(24); // 24 horas

    /**
     * Verifica se um ID já foi processado.
     *
     * @param idempotencyKey Chave única (ex: message ID, webhook ID)
     * @return true se já foi processado
     */
    public boolean isAlreadyProcessed(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            log.warn("Chave de idempotência nula ou vazia");
            return false;
        }

        String key = KEY_PREFIX + idempotencyKey;
        Boolean exists = redisTemplate.hasKey(key);

        if (Boolean.TRUE.equals(exists)) {
            log.info("Operação duplicada detectada: {}", idempotencyKey);
            return true;
        }

        return false;
    }

    /**
     * Marca um ID como já processado.
     *
     * @param idempotencyKey Chave única
     */
    public void markAsProcessed(String idempotencyKey) {
        markAsProcessed(idempotencyKey, DEFAULT_TTL);
    }

    /**
     * Marca um ID como já processado com TTL customizado.
     *
     * @param idempotencyKey Chave única
     * @param ttl Tempo de vida do registro
     */
    public void markAsProcessed(String idempotencyKey, Duration ttl) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            log.warn("Tentativa de marcar chave nula/vazia como processada");
            return;
        }

        String key = KEY_PREFIX + idempotencyKey;
        redisTemplate.opsForValue().set(key, "1", ttl);

        log.debug("Marcado como processado: {} (TTL: {})", idempotencyKey, ttl);
    }

    /**
     * Tenta processar de forma atômica (verifica E marca em uma operação).
     * Retorna true se conseguiu adquirir a "trava" (primeira vez).
     *
     * @param idempotencyKey Chave única
     * @return true se pode processar (primeira vez), false se já foi processado
     */
    public boolean tryAcquire(String idempotencyKey) {
        return tryAcquire(idempotencyKey, DEFAULT_TTL);
    }

    /**
     * Tenta processar de forma atômica com TTL customizado.
     *
     * @param idempotencyKey Chave única
     * @param ttl Tempo de vida
     * @return true se pode processar, false se duplicado
     */
    public boolean tryAcquire(String idempotencyKey, Duration ttl) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            log.warn("Chave de idempotência nula ou vazia em tryAcquire");
            return false;
        }

        String key = KEY_PREFIX + idempotencyKey;

        // SET key value NX EX ttl (atômico)
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, "1", ttl);

        if (Boolean.FALSE.equals(acquired)) {
            log.info("Operação duplicada detectada (tryAcquire): {}", idempotencyKey);
            return false;
        }

        log.debug("Lock de idempotência adquirido: {}", idempotencyKey);
        return true;
    }

    /**
     * Remove uma chave de idempotência (para testes ou retry manual).
     *
     * @param idempotencyKey Chave a remover
     */
    public void remove(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return;
        }

        String key = KEY_PREFIX + idempotencyKey;
        redisTemplate.delete(key);

        log.debug("Chave de idempotência removida: {}", idempotencyKey);
    }
}
