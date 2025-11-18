package com.chatbot.chatbotglpi.integration.glpi;

import com.chatbot.chatbotglpi.integration.glpi.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GlpiClient {

    private final RestTemplate restTemplate;



    private final GlpiPropertiesClient glpiPropertiesClient;

    /**
     * Inicia sessão no GLPI e retorna o session token
     */
    public String initSession() {
        // *** LOG DE DEBUG PARA DIAGNÓSTICO ***
        log.debug("Configuração glpiApiUrl: {}",glpiPropertiesClient.getApiUrl());

        try {
            // 1. CONSTRUÇÃO DA URL COM URI COMPONENTS BUILDER (fromUriString é a versão mais recente)
            String url = UriComponentsBuilder.fromUriString(glpiPropertiesClient.getApiUrl().trim())
                    .pathSegment("initSession")
                    .toUriString();

            // *** LOG DE DEBUG PARA DIAGNÓSTICO ***
            log.debug("URL final para initSession: {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("App-Token", glpiPropertiesClient.getApiAppToken());
            headers.set("Authorization", "user_token " + glpiPropertiesClient.getApiUserToken());

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<GlpiSessionResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    GlpiSessionResponse.class
            );

            // Tratamento de NPE: Verifica se o corpo da resposta existe
            String sessionToken = Optional.ofNullable(response.getBody())
                    .map(GlpiSessionResponse::getSessionToken)
                    .orElseThrow(() -> new RuntimeException("Resposta da API GLPI vazia ou inválida ao iniciar sessão."));

            log.debug("Sessão GLPI iniciada: {}", sessionToken);
            return sessionToken;

        } catch (Exception e) {
            log.error("Erro ao iniciar sessão GLPI", e);
            throw new RuntimeException("Falha ao autenticar no GLPI", e);
        }
    }

    /**
     * Encerra sessão no GLPI
     */
    public void killSession(String sessionToken) {
        try {
            String url = UriComponentsBuilder.fromUriString(glpiPropertiesClient.getApiUrl().trim())
                    .pathSegment("killSession")
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("App-Token", glpiPropertiesClient.getApiAppToken());
            headers.set("Session-Token", sessionToken);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            log.debug("Sessão GLPI encerrada");

        } catch (Exception e) {
            log.warn("Erro ao encerrar sessão GLPI (não crítico)", e);
        }
    }

    /**
     * Cria um ticket no GLPI
     */
    public CreateTicketResponse createTicket(CreateTicketRequest request) {
        String sessionToken = null;

        try {
            // 1. Inicia sessão
            sessionToken = initSession();

            // 2. Cria ticket
            String url = UriComponentsBuilder.fromUriString(glpiPropertiesClient.getApiUrl().trim())
                    .pathSegment("Ticket")
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("App-Token", glpiPropertiesClient.getApiAppToken());
            headers.set("Session-Token", sessionToken);

            // *** CORREÇÃO APLICADA AQUI ***
            // 1. Crie o objeto de payload que envolve sua requisição original.
            GlpiTicketPayload payload = new GlpiTicketPayload(request);

            // 2. Use o novo payload para criar a entidade da requisição.
            HttpEntity<GlpiTicketPayload> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<CreateTicketResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    CreateTicketResponse.class
            );

            CreateTicketResponse ticketResponse = Optional.ofNullable(response.getBody())
                    .orElseThrow(() -> new RuntimeException("Resposta da API GLPI vazia ou inválida ao criar ticket."));

            log.info("Ticket GLPI criado: #{}", ticketResponse.getId());
            return ticketResponse;

        } catch (Exception e) {
            log.error("Erro ao criar ticket no GLPI", e);
            throw new RuntimeException("Falha ao criar ticket no GLPI", e);

        } finally {
            // 3. Sempre encerra sessão
            if (sessionToken != null) {
                killSession(sessionToken);
            }
        }
    }

}
