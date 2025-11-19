package com.chatbot.chatbotglpi.integration.glpi.webhook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para eventos de webhook do GLPI
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlpiWebhookEvent {

    /**
     * ID do ticket
     */
    @JsonProperty("ticket_id")
    private Long ticketId;

    /**
     * Título do ticket
     */
    @JsonProperty("ticket_title")
    private String ticketTitle;

    /**
     * Status do ticket
     * 1 = Novo
     * 2 = Em atendimento
     * 3 = Planejado
     * 4 = Pendente
     * 5 = Resolvido
     * 6 = Fechado
     */
    @JsonProperty("ticket_status")
    private Integer ticketStatus;

    /**
     * Nome do status (legível)
     */
    @JsonProperty("status_name")
    private String statusName;

    /**
     * Tipo de evento
     * - ASSIGNED: Ticket atribuído a técnico
     * - IN_PROGRESS: Ticket em atendimento
     * - RESOLVED: Ticket resolvido
     * - CLOSED: Ticket fechado
     * - COMMENT_ADDED: Comentário adicionado
     */
    @JsonProperty("event_type")
    private String eventType;

    /**
     * ID do solicitante (usuário GLPI)
     */
    @JsonProperty("requester_id")
    private Integer requesterId;

    /**
     * Username do solicitante
     */
    @JsonProperty("requester_username")
    private String requesterUsername;

    /**
     * Telefone do solicitante (se disponível)
     */
    @JsonProperty("requester_phone")
    private String requesterPhone;

    /**
     * Nome do técnico atribuído (se houver)
     */
    @JsonProperty("assigned_technician")
    private String assignedTechnician;

    /**
     * Último comentário/atualização
     */
    @JsonProperty("last_comment")
    private String lastComment;

    /**
     * Data da última atualização
     */
    @JsonProperty("updated_at")
    private String updatedAt;

    /**
     * Solução do ticket (quando resolvido/fechado)
     */
    @JsonProperty("solution")
    private String solution;
}
