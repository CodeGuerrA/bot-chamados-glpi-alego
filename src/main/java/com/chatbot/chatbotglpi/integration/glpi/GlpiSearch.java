package com.chatbot.chatbotglpi.integration.glpi;

import com.chatbot.chatbotglpi.conversation.infrastrcture.metrics.BotMetrics;
import com.chatbot.chatbotglpi.integration.glpi.session.GlpiSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Serviço para buscar informações de usuários no GLPI.
 * SRP - Responsável apenas por operações de busca.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GlpiSearch {

    private final RestTemplate restTemplate;
    private final GlpiSessionManager sessionManager;
    private final GlpiPropertiesClient glpiPropertiesClient;
    private final BotMetrics botMetrics;

    /**
     * Busca ID do usuário pelo login/username
     */
    public Integer getUserIdByLogin(String username) throws Exception {
        return sessionManager.executeWithSession(sessionToken -> {
            String url = UriComponentsBuilder
                    .fromHttpUrl(glpiPropertiesClient.getApiUrl().trim())
                    .pathSegment("User")
                    .queryParam("criteria[0][field]", 2)
                    .queryParam("criteria[0][searchtype]", "equals")
                    .queryParam("criteria[0][value]", username)
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("App-Token", glpiPropertiesClient.getApiAppToken());
            headers.set("Session-Token", sessionToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ParameterizedTypeReference<List<Map<String, Object>>> typeRef = new ParameterizedTypeReference<>() {};

            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    typeRef
            );

            List<Map<String, Object>> users = response.getBody();

            if (users != null && !users.isEmpty()) {
                log.debug("GLPI retornou {} usuário(s)", users.size());

                for (Map<String, Object> user : users) {
                    String userName = (String) user.get("name");
                    log.debug("Comparando: '{}' com '{}'", username, userName);

                    if (username.equalsIgnoreCase(userName)) {
                        Object id = user.get("id");
                        if (id instanceof Number) {
                            log.info("Usuário {} encontrado com ID: {}", username, id);
                            return ((Number) id).intValue();
                        }
                    }
                }

                log.warn("Usuário {} não encontrado entre os {} resultados retornados", username, users.size());
            } else {
                log.warn("GLPI não retornou nenhum usuário para a busca: {}", username);
            }

            botMetrics.recordValidationError("username");
            return null;
        });
    }
}
