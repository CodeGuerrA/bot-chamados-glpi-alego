package com.chatbot.chatbotglpi.integration.evolution.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookEvent {

    private String event;         // Tipo de evento (ex: "messages.upsert")
    private String instance;      // Nome da instância

    // Aceita tanto array quanto objeto único usando deserializador customizado
    @JsonDeserialize(using = DataListDeserializer.class)
    private List<Data> data;     // Dados da mensagem (pode vir como array ou objeto único)

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
        if (data == null || data.isEmpty()) return null;

        Data firstData = data.get(0);
        if (firstData == null || firstData.key == null) return null;

        String remoteJid = firstData.key.remoteJid;
        if (remoteJid == null) return null;

        // Remove @s.whatsapp.net e retorna apenas o número
        return remoteJid.replace("@s.whatsapp.net", "");
    }

    // Método auxiliar para extrair o texto da mensagem
    public String getMessageText() {
        if (data == null || data.isEmpty()) return null;

        Data firstData = data.get(0);
        if (firstData == null || firstData.message == null) return null;

        // Tenta pegar o texto direto
        if (firstData.message.conversation != null) {
            return firstData.message.conversation;
        }

        // Tenta pegar de extendedTextMessage (quando é resposta)
        if (firstData.message.extendedTextMessage != null) {
            return firstData.message.extendedTextMessage.text;
        }

        return null;
    }

    // Verifica se a mensagem foi enviada pelo bot (ignora)
    public boolean isFromMe() {
        if (data == null || data.isEmpty()) return false;

        Data firstData = data.get(0);
        return firstData != null && firstData.key != null && firstData.key.fromMe;
    }

    // Método auxiliar para extrair o ID único da mensagem (para idempotência)
    public String getMessageId() {
        if (data == null || data.isEmpty()) return null;

        Data firstData = data.get(0);
        if (firstData == null || firstData.key == null) return null;

        return firstData.key.id;
    }
}