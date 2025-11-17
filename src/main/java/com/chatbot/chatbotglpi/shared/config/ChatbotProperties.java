package com.chatbot.chatbotglpi.shared.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Classe de configuração centralizada do chatbot.
 *
 * Esta classe externaliza todas as configurações e constantes do chatbot,
 * permitindo que sejam modificadas via application.properties sem necessidade
 * de recompilar o código.
 *
 * O prefixo "chatbot" significa que todas as propriedades começam com "chatbot."
 * no arquivo application.properties.
 *
 * Exemplo de uso no application.properties:
 * chatbot.conversation.ttl-minutes=30
 * chatbot.validation.title.min-length=10
 */
@Data // Gera getters e setters automaticamente
@Configuration // Marca como classe de configuração do Spring
@ConfigurationProperties(prefix = "chatbot") // Lê propriedades que começam com "chatbot."
@Validated // Ativa validação das propriedades configuradas
public class ChatbotProperties {

    /**
     * Configurações relacionadas à conversa e gestão de estado
     */
    @NotNull
    private Conversation conversation = new Conversation();

    /**
     * Configurações de validação de campos do formulário
     */
    @NotNull
    private Validation validation = new Validation();

    /**
     * Configurações do Redis e armazenamento de estado
     */
    @NotNull
    private Redis redis = new Redis();

    // ========================================================================
    // CLASSE INTERNA: Configurações da conversa
    // ========================================================================
    @Data
    public static class Conversation {

        /**
         * Tempo em minutos que uma conversa permanece ativa no Redis
         * antes de expirar automaticamente.
         *
         * Valor padrão: 30 minutos
         * Intervalo válido: 5 a 120 minutos
         */
        @Min(value = 5, message = "TTL mínimo é 5 minutos")
        @Max(value = 120, message = "TTL máximo é 120 minutos")
        private int ttlMinutes = 30;
    }

    // ========================================================================
    // CLASSE INTERNA: Configurações de validação
    // ========================================================================
    @Data
    public static class Validation {

        /**
         * Validações para o campo "título" do chamado
         */
        @NotNull
        private Title title = new Title();

        /**
         * Validações para o campo "descrição" do chamado
         */
        @NotNull
        private Description description = new Description();

        /**
         * Configurações específicas do título
         */
        @Data
        public static class Title {

            /**
             * Comprimento mínimo do título em caracteres.
             * Valor padrão: 10 caracteres
             */
            @Min(value = 5, message = "Título mínimo deve ser pelo menos 5")
            private int minLength = 10;

            /**
             * Comprimento máximo do título em caracteres.
             * Valor padrão: 100 caracteres
             */
            @Max(value = 500, message = "Título máximo não pode exceder 500")
            private int maxLength = 100;
        }

        /**
         * Configurações específicas da descrição
         */
        @Data
        public static class Description {

            /**
             * Comprimento mínimo da descrição em caracteres.
             * Valor padrão: 20 caracteres
             */
            @Min(value = 10, message = "Descrição mínima deve ser pelo menos 10")
            private int minLength = 20;

            /**
             * Comprimento máximo da descrição em caracteres.
             * Valor padrão: 1000 caracteres
             */
            @Max(value = 5000, message = "Descrição máxima não pode exceder 5000")
            private int maxLength = 1000;
        }
    }

    // ========================================================================
    // CLASSE INTERNA: Configurações do Redis
    // ========================================================================
    @Data
    public static class Redis {

        /**
         * Prefixo usado nas chaves do Redis para identificar conversas.
         *
         * Exemplo: "conversation:5511999999999"
         * Facilita identificar e filtrar chaves no Redis.
         */
        @NotNull
        private String keyPrefix = "conversation:";
    }
}
