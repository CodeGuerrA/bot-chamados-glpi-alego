package com.chatbot.chatbotglpi.conversation.api.controller;

import com.chatbot.chatbotglpi.conversation.api.dto.request.MessageRequest;
import com.chatbot.chatbotglpi.conversation.api.dto.response.MessageResponse;
import com.chatbot.chatbotglpi.conversation.application.facade.ChatbotFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST para webhooks de teste do chatbot.
 *
 * Clean Architecture:
 * - Camada de interface (API)
 * - DIP: Depende de abstrações (ports), não de implementações
 * - SRP: Expor endpoints HTTP de teste do chatbot
 */
@RestController
@RequestMapping("/api/webhook/chatbot")
@RequiredArgsConstructor
public class ChatbotWebhookController {

    private final ChatbotFacade chatbotFacade;

    /**
     * Endpoint para enviar mensagens de teste ao chatbot
     * POST /api/webhook/chatbot/message
     */
    @PostMapping("/message")
    public ResponseEntity<MessageResponse> processMessage(@Valid @RequestBody MessageRequest request) {
        String response = chatbotFacade.processMessage(
                request.getPhone(),
                request.getMessage()
        );

        return ResponseEntity.ok(MessageResponse.of(response));
    }

    /**
     * Endpoint para cancelar uma conversa de teste
     * DELETE /api/webhook/chatbot/conversation/{phone}
     */
    @DeleteMapping("/conversation/{phone}")
    public ResponseEntity<MessageResponse> cancelConversation(@PathVariable String phone) {
        String response = chatbotFacade.cancelConversation(phone);

        return ResponseEntity.ok(MessageResponse.of(response));
    }

    /**
     * Health check do chatbot de teste
     * GET /api/webhook/chatbot/health
     */
    @GetMapping("/health")
    public ResponseEntity<MessageResponse> health() {
        return ResponseEntity.ok(MessageResponse.of("OK"));
    }
}
