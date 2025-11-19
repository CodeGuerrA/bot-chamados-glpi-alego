package com.chatbot.chatbotglpi.integration.glpi.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Status de ticket do GLPI
 */
@Getter
@RequiredArgsConstructor
public enum GlpiTicketStatus {

    NOVO(1, "Novo"),
    EM_ATENDIMENTO(2, "Em Atendimento (atribuído)"),
    PLANEJADO(3, "Planejado"),
    PENDENTE(4, "Pendente"),
    RESOLVIDO(5, "Resolvido"),
    FECHADO(6, "Fechado");

    private final int code;
    private final String description;

    public static GlpiTicketStatus fromCode(int code) {
        for (GlpiTicketStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Código de status inválido: " + code);
    }
}
