package com.chatbot.chatbotglpi.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuração do OpenAPI/Swagger para documentação interativa da API.
 *
 * Acesso à documentação:
 * - Swagger UI: http://localhost:8082/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8082/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8082}")
    private String serverPort;

    @Bean
    public OpenAPI chatbotOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Chatbot GLPI - API de Webhooks")
                        .description("""
                                API de webhooks para integração do Chatbot GLPI com Evolution API e GLPI.

                                ## Funcionalidades

                                - **Webhook Evolution API**: Recebe mensagens do WhatsApp via Evolution API
                                - **Webhook GLPI**: Recebe notificações de mudanças em tickets do GLPI
                                - **Health Checks**: Endpoints para verificação de saúde da aplicação

                                ## Recursos Implementados

                                - ✅ Idempotência automática (previne duplicação)
                                - ✅ Circuit Breaker (proteção contra falhas)
                                - ✅ Rate Limiting (controle de taxa)
                                - ✅ Validações robustas
                                - ✅ Processamento assíncrono

                                ## Arquitetura

                                O sistema segue arquitetura hexagonal (Ports & Adapters) com DDD.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipe de TI - ALEGO")
                                .email("suporte-ti@alego.go.gov.br"))
                        .license(new License()
                                .name("Proprietário")
                                .url("https://www.alego.go.gov.br")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Servidor Local"),
                        new Server()
                                .url("http://seu-dominio.com")
                                .description("Servidor de Produção")
                ));
    }
}
