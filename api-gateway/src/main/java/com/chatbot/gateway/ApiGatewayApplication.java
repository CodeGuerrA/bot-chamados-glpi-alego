package com.chatbot.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway para o sistema de Chatbot GLPI.
 *
 * Responsabilidades:
 * - Roteamento de requisições
 * - Rate Limiting
 * - Autenticação e Autorização (se necessário)
 * - Circuit Breaker
 * - Logging e Monitoramento
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
