package com.chatbot.chatbotglpi.integration.evolution;

import com.chatbot.chatbotglpi.integration.evolution.dto.SendMessageRequest;
import com.chatbot.chatbotglpi.integration.evolution.dto.SendMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class EvolutionClient {

    private final RestTemplate restTemplate;


    private final EvolutionPropertiesClient evolutionPropertiesClient;

    /**
     * Envia mensagem de texto via Evolution API
     */
    public SendMessageResponse sendTextMessage(String phoneNumber, String message) {
        try {
            // Remove caracteres especiais do número (apenas dígitos)
            String cleanPhone = phoneNumber.replaceAll("[^0-9]", "");

            // Monta a URL: https://api.com/message/sendText/INSTANCE_NAME
            String url = String.format("%s/message/sendText/%s",
                    evolutionPropertiesClient.getApiUrl(),
                    evolutionPropertiesClient.getApiInstance()
            );

            // Cria request
            SendMessageRequest request = SendMessageRequest.builder()
                    .number(cleanPhone)
                    .text(message)
                    .delay(1200) // Delay de 1.2s (mais natural)
                    .build();

            // Cria headers com autenticação
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("apikey", evolutionPropertiesClient.getApiKey());

            // Cria entidade HTTP
            HttpEntity<SendMessageRequest> entity = new HttpEntity<>(request, headers);

            // Faz chamada POST
            ResponseEntity<SendMessageResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    SendMessageResponse.class
            );

            log.info("Mensagem enviada para {}: {}", cleanPhone, response.getStatusCode());
            return response.getBody();

        } catch (Exception e) {
            log.error("Erro ao enviar mensagem via Evolution API para {}: ", phoneNumber, e);
            throw new RuntimeException("Falha ao enviar mensagem WhatsApp", e);
        }
    }
}
