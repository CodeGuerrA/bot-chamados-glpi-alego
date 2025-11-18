package com.chatbot.chatbotglpi.integration.webhook;

import com.chatbot.chatbotglpi.conversation.application.facade.ChatbotFacade;
import com.chatbot.chatbotglpi.integration.evolution.dto.WebhookEvent;
import com.chatbot.chatbotglpi.integration.evolution.EvolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/webhook/evolution")
@RequiredArgsConstructor
public class EvolutionWebhookController {

    private final ChatbotFacade chatbotFacade;
    private final EvolutionService evolutionService;

    /**
     * Endpoint que recebe webhooks da Evolution API
     */
    @PostMapping
    public ResponseEntity<Void> handleWebhook(@RequestBody WebhookEvent event) {
        try {
            log.info("Webhook recebido: {}", event.getEvent());

            // Filtra apenas eventos de mensagens recebidas
            if (!"messages.upsert".equals(event.getEvent())) {
                log.debug("Evento ignorado: {}", event.getEvent());
                return ResponseEntity.ok().build();
            }

            // Ignora mensagens enviadas pelo próprio bot
            if (event.isFromMe()) {
                log.debug("Mensagem enviada pelo bot, ignorando");
                return ResponseEntity.ok().build();
            }

            // Extrai dados da mensagem
            String phone = event.getPhoneNumber();
            String message = event.getMessageText();

            if (phone == null || message == null || message.trim().isEmpty()) {
                log.warn("Webhook com dados inválidos: phone={}, message={}", phone, message);
                return ResponseEntity.ok().build();
            }

            log.info("Processando mensagem de {}: {}", phone, message);

            // Processa via ChatbotFacade
            String response = chatbotFacade.processMessage(phone, message);

            // Envia resposta via EvolutionClient (EvolutionService removido)
            evolutionService.sendMessage(phone, response);

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Erro ao processar webhook Evolution", e);
            return ResponseEntity.ok().build();
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
