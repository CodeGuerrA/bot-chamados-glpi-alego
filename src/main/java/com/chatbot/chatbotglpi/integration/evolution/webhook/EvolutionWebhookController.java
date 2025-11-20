package com.chatbot.chatbotglpi.integration.evolution.webhook;

import com.chatbot.chatbotglpi.conversation.application.facade.ChatbotFacade;
import com.chatbot.chatbotglpi.integration.evolution.dto.WebhookEvent;
import com.chatbot.chatbotglpi.integration.evolution.EvolutionService;
import com.chatbot.chatbotglpi.shared.config.WebhookSecurityConfig;
import com.chatbot.chatbotglpi.shared.idempotency.IdempotencyService;
import com.chatbot.chatbotglpi.shared.security.WebhookSignatureValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/webhook/evolution")
@RequiredArgsConstructor
public class EvolutionWebhookController {

    private final ChatbotFacade chatbotFacade;
    private final EvolutionService evolutionService;
    private final WebhookSignatureValidator signatureValidator;
    private final IdempotencyService idempotencyService;
    private final WebhookSecurityConfig securityConfig;
    private final ObjectMapper objectMapper;

    /**
     * Endpoint que recebe webhooks da Evolution API.
     *
     * Segurança implementada:
     * - Validação de assinatura HMAC-SHA256
     * - Idempotência (previne processamento duplicado)
     *
     * @param event Evento do webhook
     * @param signature Assinatura HMAC no header X-Webhook-Signature
     * @return 200 OK se processado, 401 Unauthorized se assinatura inválida
     */
    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestBody String rawPayload,
            @RequestHeader(value = "X-Webhook-Signature", required = false) String signature) {

        try {
            // 1. SEGURANÇA: Valida assinatura HMAC (se habilitado E presente)
            if (securityConfig.isEnabled()) {
                if (signature != null && !signature.isBlank()) {
                    // Se assinatura foi enviada, valida
                    String secret = securityConfig.getEvolution().getSecret();
                    boolean isValid = signatureValidator.validateSignature(rawPayload, signature, secret);

                    if (!isValid) {
                        log.error("Assinatura HMAC inválida para webhook Evolution");
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body("Invalid webhook signature");
                    }

                    log.debug("Assinatura HMAC validada com sucesso");
                } else {
                    // Evolution API não suporta assinatura HMAC nativamente
                    // Permitindo webhook sem assinatura (considere adicionar IP whitelist em produção)
                    log.warn("⚠️ Webhook Evolution recebido sem assinatura - considere adicionar IP whitelist");
                }
            } else {
                log.warn("ATENÇÃO: Validação de webhook desabilitada - NÃO USE EM PRODUÇÃO!");
            }

            // 2. Parse do payload
            WebhookEvent event = objectMapper.readValue(rawPayload, WebhookEvent.class);
            log.info("Webhook recebido: {}", event.getEvent());

            // 3. Filtra apenas eventos de mensagens recebidas
            if (!"messages.upsert".equals(event.getEvent())) {
                log.debug("Evento ignorado: {}", event.getEvent());
                return ResponseEntity.ok("Event ignored");
            }

            // 4. IDEMPOTÊNCIA: Verifica se já processou esta mensagem
            String messageId = event.getMessageId();
            if (messageId != null) {
                if (!idempotencyService.tryAcquire("webhook:evolution:" + messageId)) {
                    log.info("Mensagem duplicada detectada: {} - ignorando", messageId);
                    return ResponseEntity.ok("Duplicate message ignored");
                }
            } else {
                log.warn("Webhook sem messageId - idempotência não aplicável");
            }

            // 5. Extrai dados da mensagem
            String phone = event.getPhoneNumber();
            String message = event.getMessageText();

            if (phone == null || message == null || message.trim().isEmpty()) {
                log.warn("Webhook com dados inválidos: phone={}, message={}", phone, message);
                return ResponseEntity.ok("Invalid data");
            }

//            // 6. Ignora mensagens enviadas pelo próprio bot
//            if (event.isFromMe()) {
//                log.debug("Mensagem enviada pelo bot, ignorando");
//                return ResponseEntity.ok("Message from bot ignored");
//            }

            log.info("Processando mensagem de {}: {}", phone, message);

            // 7. Processa via ChatbotFacade
            String response = chatbotFacade.processMessage(phone, message);

            // 8. Envia resposta via EvolutionService
            evolutionService.sendMessage(phone, response);

            return ResponseEntity.ok("Message processed");

        } catch (JsonProcessingException e) {
            log.error("Erro ao fazer parse do payload JSON", e);
            return ResponseEntity.badRequest().body("Invalid JSON payload");

        } catch (Exception e) {
            log.error("Erro ao processar webhook Evolution", e);
            // Retorna 200 para não causar retry infinito
            return ResponseEntity.ok("Error processing webhook");
        }
    }

    /**
     * Health check usado pela Evolution API
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
