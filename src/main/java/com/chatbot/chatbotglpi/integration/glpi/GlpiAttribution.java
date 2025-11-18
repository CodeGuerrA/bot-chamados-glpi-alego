package com.chatbot.chatbotglpi.integration.glpi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GlpiAttribution {

    private final RestTemplate restTemplate;
    private final GlpiClient glpiClient; // Necessário para gerenciar a sessão
    private final GlpiPropertiesClient glpiPropertiesClient;
    /**
     * Atribui um ticket a um usuário no GLPI como solicitante (type=1).
     *
     * @param ticketId O ID do ticket a ser atribuído.
     * @param userId O ID do usuário a quem o ticket será atribuído.
     */
    public void assignTicketToUser(Integer ticketId, Integer userId) {
        // 1. Inicia sessão para obter o token dinâmico
        String sessionToken = glpiClient.initSession();

        try {
            // URL segura usando UriComponentsBuilder
            String url = UriComponentsBuilder
                    .fromUriString(glpiPropertiesClient.getApiUrl())
                    .pathSegment("Ticket_User")
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("App-Token", glpiPropertiesClient.getApiAppToken());
            headers.set("Session-Token", sessionToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> input = new HashMap<>();
            input.put("tickets_id", ticketId);
            input.put("users_id", userId);
            input.put("type", 1); // 1 = solicitante (requester)

            Map<String, Object> body = new HashMap<>();
            body.put("input", input);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Ticket {} atribuído ao usuário {} com sucesso. Status: {}", ticketId, userId, response.getStatusCode());
            } else {
                log.error("Falha ao atribuir ticket {}. Status: {}. Corpo da resposta: {}", ticketId, response.getStatusCode(), response.getBody());
            }

        } finally {
            // 4. Sempre encerra sessão
            glpiClient.killSession(sessionToken);
        }
    }
}
