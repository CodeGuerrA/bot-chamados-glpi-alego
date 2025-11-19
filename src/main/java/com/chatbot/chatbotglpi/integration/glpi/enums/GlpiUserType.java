package com.chatbot.chatbotglpi.integration.glpi.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Tipos de usuário no GLPI (para atribuição de ticket)
 */
@Getter
@RequiredArgsConstructor
public enum GlpiUserType {

    SOLICITANTE(1, "Solicitante/Requerente"),
    TECNICO(2, "Técnico"),
    OBSERVADOR(3, "Observador");

    private final int code;
    private final String description;

    public static GlpiUserType fromCode(int code) {
        for (GlpiUserType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Código de tipo de usuário inválido: " + code);
    }
}
