package com.chatbot.chatbotglpi.integration.glpi.webhook;

import com.chatbot.chatbotglpi.integration.glpi.webhook.dto.GlpiWebhookEvent;
import com.chatbot.chatbotglpi.shared.idempotency.IdempotencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para receber webhooks do GLPI sobre mudanças em tickets.
 * Notifica usuários proativamente via WhatsApp sobre atualizações.
 *
 * Eventos monitorados:
 * - Ticket atribuído a técnico
 * - Ticket em atendimento
 * - Ticket resolvido
 * - Ticket fechado
 * - Comentário adicionado
 *
 * Segurança:
 * - Idempotência (previne processamento duplicado)
 * - Recomenda-se IP whitelist para ambientes de produção
 */
@Slf4j
@RestController
@RequestMapping("/api/webhook/glpi")
@RequiredArgsConstructor
public class GlpiWebhookController {

    private final GlpiWebhookService webhookService;
    private final IdempotencyService idempotencyService;

    /**
     * Endpoint para receber notificações do GLPI
     *
     * Configuração no GLPI:
     * - URL: http://seu-dominio/api/webhook/glpi/notification
     * - Método: POST
     * - Content-Type: application/json
     *
     * @param event Evento do webhook
     * @return 200 OK se processado
     */
    @PostMapping("/notification")
    public ResponseEntity<String> handleTicketNotification(@RequestBody GlpiWebhookEvent event) {
        try {
            log.info("Webhook recebido do GLPI - Ticket #{} | Evento: {}",
                    event.getTicketId(), event.getEventType());

            // IDEMPOTÊNCIA: Verifica se já processou este evento
            String idempotencyKey = String.format("webhook:glpi:%s:%s",
                    event.getTicketId(), event.getEventType());

            if (!idempotencyService.tryAcquire(idempotencyKey)) {
                log.info("Evento duplicado detectado: {} - ignorando", idempotencyKey);
                return ResponseEntity.ok("Duplicate event ignored");
            }

            // Processa webhook de forma assíncrona
            webhookService.processWebhookEvent(event);

            return ResponseEntity.ok("Webhook processado com sucesso");

        } catch (Exception e) {
            log.error("Erro ao processar webhook do GLPI: ", e);
            return ResponseEntity.internalServerError()
                    .body("Erro ao processar webhook: " + e.getMessage());
        }
    }

    /**
     * Health check do endpoint de webhook
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Webhook GLPI endpoint ativo");
    }
}
