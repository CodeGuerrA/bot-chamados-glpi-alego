package com.chatbot.chatbotglpi.integration.glpi.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Tipos de ticket do GLPI
 */
@Getter
@RequiredArgsConstructor
public enum GlpiTicketType {

    INCIDENTE(1, "Incidente"),
    REQUISICAO(2, "Requisição");

    private final int code;
    private final String description;

    public static GlpiTicketType fromCode(int code) {
        for (GlpiTicketType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Código de tipo de ticket inválido: " + code);
    }
}
