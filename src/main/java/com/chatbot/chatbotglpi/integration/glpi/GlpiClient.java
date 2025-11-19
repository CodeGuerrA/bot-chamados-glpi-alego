package com.chatbot.chatbotglpi.integration.glpi;

import com.chatbot.chatbotglpi.conversation.infrastrcture.metrics.BotMetrics;
import com.chatbot.chatbotglpi.integration.glpi.dto.*;
import com.chatbot.chatbotglpi.integration.glpi.exception.GlpiInvalidResponseException;
import com.chatbot.chatbotglpi.integration.glpi.exception.GlpiTicketCreationException;
import com.chatbot.chatbotglpi.integration.glpi.session.GlpiSessionManager;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

/**
 * Cliente GLPI para operações de tickets.
 * SRP - Responsável apenas por operações de criação de tickets.
 * Gerenciamento de sessões delegado para GlpiSessionManager.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GlpiClient {

    private final RestTemplate restTemplate;
    private final GlpiPropertiesClient glpiPropertiesClient;
    private final GlpiSessionManager sessionManager;
    private final BotMetrics botMetrics;

    /**
     * Cria um ticket no GLPI usando gerenciamento automático de sessão.
     * Circuit Breaker protege contra falhas em cascata quando GLPI está indisponível.
     */
    @CircuitBreaker(name = "glpi", fallbackMethod = "createTicketFallback")
    public CreateTicketResponse createTicket(CreateTicketRequest request) throws Exception {
        return sessionManager.executeWithSession(sessionToken -> {
            try {
                String url = UriComponentsBuilder.fromHttpUrl(glpiPropertiesClient.getApiUrl().trim())
                        .pathSegment("Ticket")
                        .toUriString();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("App-Token", glpiPropertiesClient.getApiAppToken());
                headers.set("Session-Token", sessionToken);

                GlpiTicketPayload payload = new GlpiTicketPayload(request);
                HttpEntity<GlpiTicketPayload> entity = new HttpEntity<>(payload, headers);

                ResponseEntity<CreateTicketResponse> response = restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        entity,
                        CreateTicketResponse.class
                );

                CreateTicketResponse ticketResponse = Optional.ofNullable(response.getBody())
                        .orElseThrow(() -> new GlpiInvalidResponseException("Resposta da API GLPI vazia ou inválida ao criar ticket."));

                log.info("Ticket GLPI criado: #{}", ticketResponse.getId());
                return ticketResponse;

            } catch (GlpiInvalidResponseException e) {
                botMetrics.recordGlpiError("create_ticket");
                throw e;
            } catch (Exception e) {
                log.error("Erro ao criar ticket no GLPI", e);
                botMetrics.recordGlpiError("create_ticket");
                throw new GlpiTicketCreationException("Falha ao criar ticket no GLPI", e);
            }
        });
    }

    /**
     * Fallback method quando GLPI está indisponível
     */
    private CreateTicketResponse createTicketFallback(CreateTicketRequest request, Exception e) {
        log.error("Circuit Breaker ATIVO - GLPI temporariamente indisponível. Erro: {}", e.getMessage());
        botMetrics.recordGlpiError("circuit_breaker_open");
        throw new GlpiTicketCreationException("Sistema GLPI temporariamente indisponível. Por favor, tente novamente em alguns minutos.", e);
    }
}
