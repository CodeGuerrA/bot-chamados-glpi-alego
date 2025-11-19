package com.chatbot.chatbotglpi.conversation.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidade de domínio para Feedback de Tickets.
 *
 * Armazena avaliação do usuário sobre o atendimento recebido:
 * - Nota de 1 a 5 estrelas
 * - Comentário opcional
 * - Timestamp
 *
 * TODO: Quando configurar JPA, adicionar anotações:
 * @Entity
 * @Table(name = "ticket_feedback")
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketFeedback {

    /**
     * ID único do feedback
     * TODO: Adicionar @Id @GeneratedValue quando JPA estiver configurado
     */
    private Long id;

    /**
     * ID do ticket relacionado (vindo do GLPI)
     */
    private Long ticketId;

    /**
     * Telefone do usuário que deu o feedback
     */
    private String userPhone;

    /**
     * Username do usuário (se disponível)
     */
    private String username;

    /**
     * Nota de 1 a 5 estrelas
     * 1 = Muito ruim
     * 2 = Ruim
     * 3 = Regular
     * 4 = Bom
     * 5 = Excelente
     */
    private Integer rating;

    /**
     * Comentário opcional do usuário
     */
    private String comment;

    /**
     * Sentimento analisado do comentário
     * POSITIVE, NEGATIVE, NEUTRAL
     */
    private String sentiment;

    /**
     * Data e hora do feedback
     */
    private LocalDateTime feedbackDate;

    /**
     * Categoria do ticket (se disponível)
     */
    private String ticketCategory;

    /**
     * Técnico que atendeu (se disponível)
     */
    private String assignedTechnician;

    /**
     * Valida se o rating está no intervalo válido
     */
    public boolean isValidRating() {
        return rating != null && rating >= 1 && rating <= 5;
    }

    /**
     * Retorna se o feedback é positivo (4-5 estrelas)
     */
    public boolean isPositive() {
        return rating != null && rating >= 4;
    }

    /**
     * Retorna se o feedback é negativo (1-2 estrelas)
     */
    public boolean isNegative() {
        return rating != null && rating <= 2;
    }

    /**
     * Retorna representação em estrelas
     */
    public String getStarRepresentation() {
        if (rating == null) return "";
        return "⭐".repeat(rating);
    }
}
