package com.chatbot.chatbotglpi.integration.glpi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class GlpiSearch {

    private final RestTemplate restTemplate;
    private final GlpiClient glpiClient;


    private final GlpiPropertiesClient glpiPropertiesClient;
    public Integer getUserIdByLogin(String username) {
        String sessionToken = glpiClient.initSession();

        try {
            // URL segura usando UriComponentsBuilder
            String url = UriComponentsBuilder
                    .fromUriString(glpiPropertiesClient.getApiUrl())
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

            if (users != null) {
                for (Map<String, Object> user : users) {
                    if (Objects.equals(username, user.get("name"))) {
                        Object id = user.get("id");
                        if (id instanceof Number) {
                            return ((Number) id).intValue();
                        }
                    }
                }
            }

            log.warn("Usuário {} não encontrado.", username);
            return null;

        } finally {
            glpiClient.killSession(sessionToken);
        }
    }
}
