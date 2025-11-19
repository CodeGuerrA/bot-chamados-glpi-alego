package com.chatbot.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Fallback Controller para requisições quando serviços estão indisponíveis.
 * Fornece respostas amigáveis ao usuário durante falhas.
 */
@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/chatbot")
    @PostMapping("/chatbot")
    public ResponseEntity<Map<String, Object>> chatbotFallback() {
        log.warn("Circuit Breaker ATIVO - Chatbot Service está temporariamente indisponível");

        Map<String, Object> response = Map.of(
            "status", "SERVICE_UNAVAILABLE",
            "message", "O serviço de chatbot está temporariamente indisponível. Por favor, tente novamente em alguns momentos.",
            "timestamp", LocalDateTime.now().toString(),
            "retry_after_seconds", 30
        );

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }

    @GetMapping("/webhook")
    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> webhookFallback() {
        log.warn("Circuit Breaker ATIVO - Webhook está temporariamente indisponível");

        Map<String, Object> response = Map.of(
            "status", "SERVICE_UNAVAILABLE",
            "message", "O serviço de webhook está temporariamente indisponível. A mensagem será processada assim que o serviço voltar.",
            "timestamp", LocalDateTime.now().toString()
        );

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }
}
