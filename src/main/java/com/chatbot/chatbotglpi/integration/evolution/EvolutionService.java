package com.chatbot.chatbotglpi.integration.evolution;
import com.chatbot.chatbotglpi.integration.evolution.dto.SendMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvolutionService {

    private final EvolutionClient evolutionClient;

    /**
     * Envia mensagem com retry automático em caso de falha
     */
    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void sendMessage(String phoneNumber, String message) {
        try {
            SendMessageResponse response = evolutionClient.sendTextMessage(phoneNumber, message);
            log.debug("Mensagem enviada com sucesso: {}", response);
        } catch (Exception e) {
            log.error("Falha ao enviar mensagem após retries", e);
            // Aqui você pode decidir se lança exceção ou registra em fila de retry
            throw e;
        }
    }
}
