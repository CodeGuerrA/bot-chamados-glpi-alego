package com.chatbot.chatbotglpi.shared.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configurações de segurança para webhooks.
 *
 * Carrega secrets e configurações do application.properties.
 * HMAC habilitado apenas para Evolution API.
 */
@Getter
@Configuration
@ConfigurationProperties(prefix = "webhook.security")
public class WebhookSecurityConfig {

    /**
     * Configurações específicas da Evolution API
     */
    private final Evolution evolution = new Evolution();

    /**
     * Se true, valida assinatura HMAC dos webhooks (padrão: true)
     * IMPORTANTE: NUNCA desabilite em produção!
     */
    private boolean enabled = true;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Getter
    public static class Evolution {
        /**
         * Chave secreta compartilhada com Evolution API para HMAC-SHA256
         * Deve ser a mesma configurada no Evolution (variável WEBHOOK_SECRET)
         */
        private String secret;

        public void setSecret(String secret) {
            this.secret = secret;
        }
    }
}
