package com.chatbot.chatbotglpi.integration.glpi;

import com.chatbot.chatbotglpi.integration.glpi.enums.GlpiUserType;
import com.chatbot.chatbotglpi.integration.glpi.session.GlpiSessionManager;
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

/**
 * Serviço para atribuir tickets a usuários no GLPI.
 * SRP - Responsável apenas por atribuições de tickets.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GlpiAttribution {

    private final RestTemplate restTemplate;
    private final GlpiSessionManager sessionManager;
    private final GlpiPropertiesClient glpiPropertiesClient;
    /**
     * Atribui um ticket a um usuário no GLPI como solicitante.
     * Usa gerenciamento automático de sessão.
     *
     * @param ticketId O ID do ticket a ser atribuído.
     * @param userId O ID do usuário a quem o ticket será atribuído.
     */
    public void assignTicketToUser(Integer ticketId, Integer userId) throws Exception {
        sessionManager.executeWithSession(sessionToken -> {
            String url = UriComponentsBuilder
                    .fromHttpUrl(glpiPropertiesClient.getApiUrl().trim())
                    .pathSegment("Ticket_User")
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("App-Token", glpiPropertiesClient.getApiAppToken());
            headers.set("Session-Token", sessionToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> input = new HashMap<>();
            input.put("tickets_id", ticketId);
            input.put("users_id", userId);
            input.put("type", GlpiUserType.SOLICITANTE.getCode());

            Map<String, Object> body = new HashMap<>();
            body.put("input", input);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Ticket {} atribuído ao usuário {} com sucesso", ticketId, userId);
            } else {
                log.error("Falha ao atribuir ticket {}. Status: {}", ticketId, response.getStatusCode());
            }

            return null; // Void operation
        });
    }
}
