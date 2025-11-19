package com.chatbot.chatbotglpi.conversation.infrastrcture.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Serviço de métricas customizadas do chatbot para Prometheus.
 *
 * Expõe métricas de negócio através do endpoint /actuator/prometheus
 * para monitoramento e criação de dashboards no Grafana.
 *
 * Métricas disponíveis:
 * - chatbot_conversations_started_total: Total de conversas iniciadas
 * - chatbot_conversations_completed_total: Total de tickets criados
 * - chatbot_conversations_cancelled_total: Total de conversas canceladas
 * - chatbot_conversations_timeout_total: Total de timeouts por inatividade
 * - chatbot_validation_errors_total: Erros de validação por tipo
 * - chatbot_conversation_duration: Tempo de duração das conversas
 * - chatbot_glpi_errors_total: Erros na integração com GLPI
 * - chatbot_evolution_errors_total: Erros na integração com Evolution API
 */
@Slf4j
@Component
public class BotMetrics {

    private final Counter conversationsStartedCounter;
    private final Counter conversationsCompletedCounter;
    private final Counter conversationsCancelledCounter;
    private final Counter conversationsTimeoutCounter;
    private final Counter validationErrorsCounter;
    private final Counter glpiErrorsCounter;
    private final Counter evolutionErrorsCounter;
    private final Timer conversationDurationTimer;
    private final MeterRegistry meterRegistry;

    public BotMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        // Contador de conversas iniciadas
        this.conversationsStartedCounter = Counter.builder("chatbot.conversations.started")
                .description("Total de conversas iniciadas")
                .tag("type", "greeting")
                .register(meterRegistry);

        // Contador de conversas completas (tickets criados)
        this.conversationsCompletedCounter = Counter.builder("chatbot.conversations.completed")
                .description("Total de conversas completadas com sucesso (tickets criados)")
                .tag("type", "ticket_created")
                .register(meterRegistry);

        // Contador de conversas canceladas
        this.conversationsCancelledCounter = Counter.builder("chatbot.conversations.cancelled")
                .description("Total de conversas canceladas pelo usuário")
                .tag("type", "user_cancel")
                .register(meterRegistry);

        // Contador de timeouts
        this.conversationsTimeoutCounter = Counter.builder("chatbot.conversations.timeout")
                .description("Total de conversas encerradas por timeout de inatividade")
                .tag("type", "inactivity")
                .register(meterRegistry);

        // Contador de erros de validação
        this.validationErrorsCounter = Counter.builder("chatbot.validation.errors")
                .description("Total de erros de validação")
                .tag("error_type", "validation")
                .register(meterRegistry);

        // Contador de erros GLPI
        this.glpiErrorsCounter = Counter.builder("chatbot.integration.glpi.errors")
                .description("Total de erros na integração com GLPI")
                .tag("integration", "glpi")
                .register(meterRegistry);

        // Contador de erros Evolution API
        this.evolutionErrorsCounter = Counter.builder("chatbot.integration.evolution.errors")
                .description("Total de erros na integração com Evolution API")
                .tag("integration", "evolution")
                .register(meterRegistry);

        // Timer para duração de conversas
        this.conversationDurationTimer = Timer.builder("chatbot.conversation.duration")
                .description("Tempo de duração das conversas do início até conclusão")
                .publishPercentiles(0.5, 0.95, 0.99) // P50, P95, P99
                .register(meterRegistry);

        log.info("Métricas customizadas do chatbot inicializadas com sucesso");
    }

    /**
     * Incrementa contador de conversas iniciadas
     */
    public void recordConversationStarted() {
        conversationsStartedCounter.increment();
        log.debug("Métrica: Conversa iniciada registrada");
    }

    /**
     * Incrementa contador de conversas completas (tickets criados)
     */
    public void recordConversationCompleted() {
        conversationsCompletedCounter.increment();
        log.debug("Métrica: Conversa completada registrada");
    }

    /**
     * Incrementa contador de conversas canceladas
     */
    public void recordConversationCancelled() {
        conversationsCancelledCounter.increment();
        log.debug("Métrica: Conversa cancelada registrada");
    }

    /**
     * Incrementa contador de timeouts
     */
    public void recordConversationTimeout() {
        conversationsTimeoutCounter.increment();
        log.debug("Métrica: Timeout registrado");
    }

    /**
     * Incrementa contador de erros de validação
     *
     * @param validationType Tipo de validação que falhou (username, description, etc)
     */
    public void recordValidationError(String validationType) {
        Counter.builder("chatbot.validation.errors")
                .description("Erros de validação por tipo")
                .tag("validation_type", validationType)
                .register(meterRegistry)
                .increment();

        log.debug("Métrica: Erro de validação registrado - tipo: {}", validationType);
    }

    /**
     * Incrementa contador de erros GLPI
     *
     * @param operation Operação que falhou (create_ticket, search_user, etc)
     */
    public void recordGlpiError(String operation) {
        Counter.builder("chatbot.integration.glpi.errors")
                .description("Erros na integração com GLPI")
                .tag("operation", operation)
                .register(meterRegistry)
                .increment();

        log.debug("Métrica: Erro GLPI registrado - operação: {}", operation);
    }

    /**
     * Incrementa contador de erros Evolution API
     *
     * @param operation Operação que falhou (send_message, etc)
     */
    public void recordEvolutionError(String operation) {
        Counter.builder("chatbot.integration.evolution.errors")
                .description("Erros na integração com Evolution API")
                .tag("operation", operation)
                .register(meterRegistry)
                .increment();

        log.debug("Métrica: Erro Evolution API registrado - operação: {}", operation);
    }

    /**
     * Registra duração de uma conversa completa
     *
     * @param startTime Timestamp do início da conversa
     * @param endTime Timestamp do fim da conversa
     */
    public void recordConversationDuration(LocalDateTime startTime, LocalDateTime endTime) {
        Duration duration = Duration.between(startTime, endTime);
        conversationDurationTimer.record(duration);

        log.debug("Métrica: Duração de conversa registrada - {}s", duration.toSeconds());
    }

    /**
     * Registra duração de uma conversa em segundos (alternativa)
     *
     * @param durationSeconds Duração em segundos
     */
    public void recordConversationDuration(long durationSeconds) {
        conversationDurationTimer.record(Duration.ofSeconds(durationSeconds));
        log.debug("Métrica: Duração de conversa registrada - {}s", durationSeconds);
    }

    /**
     * Retorna total de conversas iniciadas (para debugging)
     */
    public double getConversationsStartedCount() {
        return conversationsStartedCounter.count();
    }

    /**
     * Retorna total de conversas completas (para debugging)
     */
    public double getConversationsCompletedCount() {
        return conversationsCompletedCounter.count();
    }

    /**
     * Retorna taxa de sucesso (percentual de conversas que resultaram em ticket)
     */
    public double getSuccessRate() {
        double started = conversationsStartedCounter.count();
        double completed = conversationsCompletedCounter.count();

        if (started == 0) return 0.0;
        return (completed / started) * 100.0;
    }

    /**
     * Retorna taxa de cancelamento
     */
    public double getCancellationRate() {
        double started = conversationsStartedCounter.count();
        double cancelled = conversationsCancelledCounter.count();

        if (started == 0) return 0.0;
        return (cancelled / started) * 100.0;
    }
}
