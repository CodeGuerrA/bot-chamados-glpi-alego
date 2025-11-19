package com.chatbot.chatbotglpi.integration.glpi.webhook;

import com.chatbot.chatbotglpi.conversation.application.service.FeedbackService;
import com.chatbot.chatbotglpi.integration.evolution.EvolutionService;
import com.chatbot.chatbotglpi.integration.glpi.webhook.dto.GlpiWebhookEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Serviço para processar webhooks do GLPI e enviar notificações proativas
 * aos usuários via WhatsApp sobre atualizações em seus tickets.
 *
 * SRP - Responsável apenas por processar webhooks e coordenar notificações.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GlpiWebhookService {

    private final EvolutionService evolutionService;
    private final FeedbackService feedbackService;

    /**
     * Processa evento de webhook de forma assíncrona.
     * Não bloqueia a resposta ao GLPI.
     */
    @Async
    public void processWebhookEvent(GlpiWebhookEvent event) {
        try {
            log.info("Processando webhook - Ticket #{} | Tipo: {}",
                    event.getTicketId(), event.getEventType());

            // Valida se temos telefone do solicitante
            if (event.getRequesterPhone() == null || event.getRequesterPhone().isBlank()) {
                log.warn("Webhook ignorado - Ticket #{} não possui telefone do solicitante",
                        event.getTicketId());
                return;
            }

            // Monta mensagem baseada no tipo de evento
            String message = buildNotificationMessage(event);

            // Envia notificação via WhatsApp
            evolutionService.sendMessage(event.getRequesterPhone(), message);

            log.info("Notificação enviada com sucesso - Ticket #{} para {}",
                    event.getTicketId(), event.getRequesterPhone());

        } catch (Exception e) {
            log.error("Erro ao processar webhook - Ticket #{}: ", event.getTicketId(), e);
        }
    }

    /**
     * Constrói mensagem de notificação baseada no tipo de evento
     */
    private String buildNotificationMessage(GlpiWebhookEvent event) {
        return switch (event.getEventType()) {
            case "ASSIGNED" -> buildAssignedMessage(event);
            case "IN_PROGRESS" -> buildInProgressMessage(event);
            case "RESOLVED" -> buildResolvedMessage(event);
            case "CLOSED" -> buildClosedMessage(event);
            case "COMMENT_ADDED" -> buildCommentMessage(event);
            default -> buildGenericUpdateMessage(event);
        };
    }

    /**
     * Mensagem quando ticket é atribuído a um técnico
     */
    private String buildAssignedMessage(GlpiWebhookEvent event) {
        return String.format("""
                👤 *Chamado Atribuído!*

                Seu chamado *#%d* foi atribuído para atendimento.

                📋 *Título:* %s
                👨‍💻 *Técnico:* %s

                Em breve você será atendido!

                📞 *Dúvidas?* Ligue no ramal *3018*
                """,
                event.getTicketId(),
                event.getTicketTitle(),
                event.getAssignedTechnician() != null ? event.getAssignedTechnician() : "Em definição");
    }

    /**
     * Mensagem quando ticket entra em atendimento
     */
    private String buildInProgressMessage(GlpiWebhookEvent event) {
        String message = String.format("""
                ⚙️ *Atendimento Iniciado!*

                Seu chamado *#%d* está sendo atendido.

                📋 *Título:* %s
                👨‍💻 *Técnico:* %s
                """,
                event.getTicketId(),
                event.getTicketTitle(),
                event.getAssignedTechnician() != null ? event.getAssignedTechnician() : "Equipe de TI");

        if (event.getLastComment() != null && !event.getLastComment().isBlank()) {
            message += String.format("""

                    💬 *Observação do técnico:*
                    %s
                    """, event.getLastComment());
        }

        message += """

                📞 *Precisa falar com o técnico?* Ramal *3018*
                """;

        return message;
    }

    /**
     * Mensagem quando ticket é resolvido
     */
    private String buildResolvedMessage(GlpiWebhookEvent event) {
        String message = String.format("""
                ✅ *Chamado Resolvido!*

                Seu chamado *#%d* foi resolvido.

                📋 *Título:* %s
                👨‍💻 *Técnico:* %s
                """,
                event.getTicketId(),
                event.getTicketTitle(),
                event.getAssignedTechnician() != null ? event.getAssignedTechnician() : "Equipe de TI");

        if (event.getSolution() != null && !event.getSolution().isBlank()) {
            message += String.format("""

                    🔧 *Solução aplicada:*
                    %s
                    """, event.getSolution());
        }

        message += """

                ❓ *O problema foi resolvido?*
                Se não foi resolvido, entre em contato no ramal *3018*

                ━━━━━━━━━━━━━━━━━━━━━━━━

                """ + feedbackService.buildFeedbackRequestMessage(event.getTicketId(), event.getTicketTitle());

        return message;
    }

    /**
     * Mensagem quando ticket é fechado
     */
    private String buildClosedMessage(GlpiWebhookEvent event) {
        return String.format("""
                🔒 *Chamado Fechado*

                Seu chamado *#%d* foi finalizado.

                📋 *Título:* %s

                Obrigado por usar nosso serviço de suporte!

                💬 *Precisa de ajuda novamente?*
                Digite *oi* para abrir um novo chamado

                📞 *Suporte:* Ramal *3018*
                """,
                event.getTicketId(),
                event.getTicketTitle());
    }

    /**
     * Mensagem quando comentário é adicionado
     */
    private String buildCommentMessage(GlpiWebhookEvent event) {
        return String.format("""
                💬 *Novo Comentário no seu Chamado*

                Chamado *#%d*: %s

                📝 *Mensagem do técnico:*
                %s

                📞 *Precisa responder?* Ligue no ramal *3018*
                """,
                event.getTicketId(),
                event.getTicketTitle(),
                event.getLastComment() != null ? event.getLastComment() : "Sem detalhes");
    }

    /**
     * Mensagem genérica para outros tipos de atualização
     */
    private String buildGenericUpdateMessage(GlpiWebhookEvent event) {
        return String.format("""
                🔔 *Atualização no seu Chamado*

                Seu chamado *#%d* foi atualizado.

                📋 *Título:* %s
                📊 *Status:* %s

                📞 *Mais informações:* Ramal *3018*
                """,
                event.getTicketId(),
                event.getTicketTitle(),
                event.getStatusName() != null ? event.getStatusName() : "Em andamento");
    }
}
