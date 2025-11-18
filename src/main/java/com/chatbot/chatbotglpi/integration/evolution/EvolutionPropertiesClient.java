package com.chatbot.chatbotglpi.integration.evolution;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "evolution")
public class EvolutionPropertiesClient {
    //O Spring Boot faz a conversão do formato dot-case (separado por pontos, como nos arquivos de configuração)
    // para o formato camelCase (usado nas variáveis Java):
    private String apiUrl;
    private String apiKey;
    private String apiInstance;
}
