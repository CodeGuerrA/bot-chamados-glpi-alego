package com.chatbot.chatbotglpi.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuração do Swagger/OpenAPI para documentação interativa da API.
 *
 * Swagger (OpenAPI) é uma especificação padrão da indústria para documentar APIs REST.
 * Ele gera automaticamente uma interface web interativa onde você pode:
 * - Visualizar todos os endpoints da API
 * - Ver modelos de dados (DTOs)
 * - Testar requisições diretamente pelo navegador
 * - Exportar especificação para integração com outras ferramentas
 *
 * COMO ACESSAR:
 * - UI Interativo: http://localhost:8080/swagger-ui.html
 * - Especificação JSON: http://localhost:8080/v3/api-docs
 * - Especificação YAML: http://localhost:8080/v3/api-docs.yaml
 *
 * BENEFÍCIOS:
 * - ✅ Documentação sempre atualizada (gerada do código)
 * - ✅ Testes de API sem Postman/Insomnia
 * - ✅ Facilita integração para outros desenvolvedores
 * - ✅ Geração de clientes em várias linguagens (swagger-codegen)
 * - ✅ Padrão da indústria (compatível com ferramentas)
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configuração principal do OpenAPI.
     *
     * Este bean define metadados da API que aparecem na documentação Swagger.
     *
     * @return Configuração do OpenAPI
     */
    @Bean
    public OpenAPI chatbotOpenAPI() {
        return new OpenAPI()
                // Informações gerais da API
                .info(new Info()
                        .title("Chatbot GLPI - API de Suporte Guiado")
                        .description("""
                                API REST para chatbot conversacional de abertura de chamados de suporte.

                                ## Funcionalidades

                                - 🤖 Conversa guiada para criação de tickets
                                - 💾 Gerenciamento de estado com Redis + Cache L2 (Caffeine)
                                - 🛡️ Validações robustas e sanitização de inputs
                                - 📊 Métricas e health checks via Actuator
                                - 🔄 Suporte a múltiplos estados conversacionais

                                ## Fluxo Conversacional

                                1. **GREETING** - Saudação inicial
                                2. **COLLECTING_DESCRIPTION** - Coleta descrição detalhada (título gerado automaticamente)
                                3. **COLLECTING_LOCATION** - Coleta local do problema (sala, gabinete)
                                4. **COLLECTING_RAMAL** - Coleta ramal do usuário
                                5. **CONFIRMING** - Confirmação do chamado
                                6. **COMPLETED** - Ticket criado

                                <!-- Futura implementação:
                                - **COLLECTING_CATEGORY** - Escolha categoria (Hardware/Software/Rede/Acesso)
                                - **COLLECTING_URGENCY** - Define urgência (Baixa/Média/Alta/Crítica)
                                -->

                                ## Comandos Especiais

                                - `voltar` - Retorna à etapa anterior
                                - `voltar <campo>` - Edita campo específico na confirmação
                                - `cancelar` - Cancela conversa via endpoint DELETE

                                ## Tecnologias

                                - Java 21
                                - Spring Boot 3.5.6
                                - Redis (armazenamento de sessão)
                                - Caffeine (cache L2 local)
                                - Jakarta Validation (Bean Validation)
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipe de Desenvolvimento")
                                .email("dev@chatbot.com")
                                .url("https://github.com/seu-usuario/chatbot-glpi"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))

                // Servidores disponíveis
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de Desenvolvimento Local"),
                        new Server()
                                .url("https://chatbot.producao.com")
                                .description("Servidor de Produção (exemplo)")
                ));
    }

    /**
     * CUSTOMIZAÇÃO ADICIONAL:
     *
     * Você pode customizar ainda mais o Swagger adicionando:
     *
     * 1. Segurança (JWT, OAuth2):
     *    .components(new Components()
     *        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
     *            .type(SecurityScheme.Type.HTTP)
     *            .scheme("bearer")
     *            .bearerFormat("JWT")))
     *
     * 2. Tags customizadas:
     *    .tags(List.of(
     *        new Tag().name("Webhook").description("Endpoints de webhook"),
     *        new Tag().name("Admin").description("Endpoints administrativos")))
     *
     * 3. External Docs:
     *    .externalDocs(new ExternalDocumentation()
     *        .description("Documentação Completa")
     *        .url("https://docs.chatbot.com"))
     */
}
