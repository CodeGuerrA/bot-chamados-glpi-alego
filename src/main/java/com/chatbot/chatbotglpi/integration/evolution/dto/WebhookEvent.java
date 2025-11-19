package com.chatbot.chatbotglpi.integration.evolution.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookEvent {

    private String event;         // Tipo de evento (ex: "messages.upsert")
    private String instance;      // Nome da instância
    private Data data;           // Dados da mensagem

    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {
        private Key key;
        private Message message;

        @lombok.Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Key {
            private String remoteJid;     // Número do remetente (5511999999999@s.whatsapp.net)
            private boolean fromMe;       // true se foi enviado pelo bot
            private String id;            // ID da mensagem
        }

        @lombok.Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Message {
            private String conversation;  // Texto da mensagem

            @JsonProperty("extendedTextMessage")
            private ExtendedText extendedTextMessage;

            @lombok.Data
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class ExtendedText {
                private String text;      // Texto quando é resposta/citação
            }
        }
    }

    // Método auxiliar para extrair o número de telefone
    public String getPhoneNumber() {
        if (data == null || data.key == null) return null;

        String remoteJid = data.key.remoteJid;
        // Remove @s.whatsapp.net e retorna apenas o número
        return remoteJid.replace("@s.whatsapp.net", "");
    }

    // Método auxiliar para extrair o texto da mensagem
    public String getMessageText() {
        if (data == null || data.message == null) return null;

        // Tenta pegar o texto direto
        if (data.message.conversation != null) {
            return data.message.conversation;
        }

        // Tenta pegar de extendedTextMessage (quando é resposta)
        if (data.message.extendedTextMessage != null) {
            return data.message.extendedTextMessage.text;
        }

        return null;
    }

    // Verifica se a mensagem foi enviada pelo bot (ignora)
    public boolean isFromMe() {
        return data != null && data.key != null && data.key.fromMe;
    }

    // Método auxiliar para extrair o ID único da mensagem (para idempotência)
    public String getMessageId() {
        if (data == null || data.key == null) return null;
        return data.key.id;
    }
}