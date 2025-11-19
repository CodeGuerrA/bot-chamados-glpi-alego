package com.chatbot.chatbotglpi.integration.glpi.session;

import com.chatbot.chatbotglpi.integration.glpi.GlpiPropertiesClient;
import com.chatbot.chatbotglpi.integration.glpi.dto.GlpiSessionResponse;
import com.chatbot.chatbotglpi.integration.glpi.exception.GlpiAuthenticationException;
import com.chatbot.chatbotglpi.integration.glpi.exception.GlpiInvalidResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

/**
 * Gerenciador de sessões GLPI.
 * SRP - Única responsabilidade: gerenciar ciclo de vida de sessões.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GlpiSessionManager {

    private final RestTemplate restTemplate;
    private final GlpiPropertiesClient glpiPropertiesClient;

    /**
     * Inicia sessão no GLPI e retorna o session tokens
     */
    public String initSession() {
        // *** LOG DE DEBUG PARA DIAGNÓSTICO ***
        log.debug("Configuração glpiApiUrl: {}",glpiPropertiesClient.getApiUrl());

        try {
            // 1. CONSTRUÇÃO DA URL COM URI COMPONENTS BUILDER (fromHttpUrl para URLs completas)
            String url = UriComponentsBuilder.fromHttpUrl(glpiPropertiesClient.getApiUrl().trim())
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
            String url = UriComponentsBuilder.fromHttpUrl(glpiPropertiesClient.getApiUrl().trim())
                    .pathSegment("killSession")
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("App-Token", glpiPropertiesClient.getApiAppToken());
            headers.set("Session-Token", sessionToken);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            log.debug("Sessão GLPI encerrada com sucesso");

        } catch (Exception e) {
            log.warn("Erro ao encerrar sessão GLPI (não crítico)", e);
        }
    }

    /**
     * Executa operação com sessão GLPI (Template Method Pattern).
     * Garante que a sessão sempre será encerrada.
     */
    public <T> T executeWithSession(GlpiSessionOperation<T> operation) throws Exception {
        String sessionToken = null;
        try {
            sessionToken = initSession();
            return operation.execute(sessionToken);
        } finally {
            if (sessionToken != null) {
                killSession(sessionToken);
            }
        }
    }

    @FunctionalInterface
    public interface GlpiSessionOperation<T> {
        T execute(String sessionToken) throws Exception;
    }
}
