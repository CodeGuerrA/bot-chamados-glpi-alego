package com.chatbot.chatbotglpi.conversation.infrastrcture.scheduler;

import com.chatbot.chatbotglpi.conversation.application.port.output.ConversationStateRepository;
import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import com.chatbot.chatbotglpi.conversation.infrastrcture.metrics.BotMetrics;
import com.chatbot.chatbotglpi.integration.evolution.EvolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler para verificar e limpar conversas inativas.
 * Melhora UX evitando que usuários fiquem com conversas "penduradas".
 *
 * Configuração:
 * - Executa a cada 5 minutos
 * - Considera inativa: conversa sem atividade há mais de 10 minutos
 * - Ação: Envia mensagem de aviso e remove do Redis
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InactivityTimeoutScheduler {

    private final ConversationStateRepository conversationRepository;
    private final EvolutionService evolutionService;
    private final BotMetrics botMetrics;

    private static final int INACTIVITY_THRESHOLD_MINUTES = 10;

    /**
     * Verifica conversas inativas a cada 5 minutos
     */
    @Scheduled(fixedRate = 300000) // 5 minutos = 300.000ms
    public void checkInactiveConversations() {
        log.debug("Iniciando verificação de conversas inativas...");

        try {
            List<ConversationState> allConversations = conversationRepository.findAll();

            if (allConversations.isEmpty()) {
                log.debug("Nenhuma conversa ativa para verificar");
                return;
            }

            log.info("Verificando {} conversas ativas", allConversations.size());

            int timeoutCount = 0;
            LocalDateTime now = LocalDateTime.now();

            for (ConversationState state : allConversations) {
                if (isInactive(state, now)) {
                    handleInactiveConversation(state);
                    timeoutCount++;
                }
            }

            if (timeoutCount > 0) {
                log.info("Total de conversas removidas por inatividade: {}", timeoutCount);
            } else {
                log.debug("Nenhuma conversa inativa encontrada");
            }

        } catch (Exception e) {
            log.error("Erro ao verificar conversas inativas", e);
        }
    }

    /**
     * Verifica se a conversa está inativa
     */
    private boolean isInactive(ConversationState state, LocalDateTime now) {
        if (state.getLastActivity() == null) {
            log.warn("Conversa sem lastActivity: {}", state.getPhone());
            return false;
        }

        Duration duration = Duration.between(state.getLastActivity(), now);
        long minutesInactive = duration.toMinutes();

        return minutesInactive >= INACTIVITY_THRESHOLD_MINUTES;
    }

    /**
     * Trata conversa inativa: envia mensagem e remove
     */
    private void handleInactiveConversation(ConversationState state) {
        try {
            String phone = state.getPhone();
            long minutesInactive = Duration.between(state.getLastActivity(), LocalDateTime.now()).toMinutes();

            log.info("Conversa inativa detectada: {} ({}min sem atividade)", phone, minutesInactive);

            // Envia mensagem de aviso ao usuário
            String message = String.format("""
                    ⏰ *Conversa pausada por inatividade*

                    Percebi que você ficou %d minutos sem responder.

                    Por segurança, pausei nossa conversa e removi seus dados temporários.

                    📞 *Precisa de ajuda?*
                    Ligue para o suporte no ramal *3018*

                    💬 *Quer abrir um chamado?*
                    Digite *oi* para começar uma nova conversa!
                    """, minutesInactive);

            evolutionService.sendMessage(phone, message);
            log.info("Mensagem de timeout enviada para {}", phone);

            // Remove conversa do Redis
            conversationRepository.delete(phone);
            log.info("Conversa removida: {}", phone);

            // Registra métrica de timeout
            botMetrics.recordConversationTimeout();

        } catch (Exception e) {
            log.error("Erro ao processar conversa inativa: {}", state.getPhone(), e);
        }
    }
}
