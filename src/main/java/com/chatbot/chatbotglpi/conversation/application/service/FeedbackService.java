package com.chatbot.chatbotglpi.conversation.application.service;

import com.chatbot.chatbotglpi.conversation.domain.entity.TicketFeedback;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Serviço para processar feedback dos usuários sobre atendimento de tickets.
 *
 * IMPORTANTE: Feedback é salvo no GLPI, NÃO localmente.
 * Este serviço apenas valida e formata mensagens.
 *
 * Feedback coletado após ticket ser resolvido/fechado:
 * - Nota de 1 a 5 estrelas
 * - Comentário opcional
 * - Enviado ao GLPI via API
 *
 * SRP - Responsável apenas por validação e formatação de feedback.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackService {

    /**
     * Valida se a entrada é uma nota válida (1-5)
     */
    public boolean isValidRating(String input) {
        if (input == null || input.isBlank()) {
            return false;
        }

        try {
            int rating = Integer.parseInt(input.trim());
            return rating >= 1 && rating <= 5;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Processa feedback do usuário.
     *
     * NOTA: Feedback será enviado ao GLPI via API (não salva localmente).
     *
     * @param ticketId ID do ticket
     * @param phone Telefone do usuário
     * @param ratingInput Nota digitada (1-5)
     * @param comment Comentário opcional
     * @return TicketFeedback para enviar ao GLPI
     */
    public TicketFeedback processFeedback(Long ticketId, String phone, String ratingInput, String comment) {
        int rating = Integer.parseInt(ratingInput.trim());

        log.info("Feedback recebido - Ticket #{} | Telefone: {} | Nota: {}/5",
                ticketId, phone, rating);

        // Cria objeto de feedback para enviar ao GLPI
        TicketFeedback feedback = TicketFeedback.builder()
                .ticketId(ticketId)
                .userPhone(phone)
                .rating(rating)
                .comment(comment)
                .feedbackDate(LocalDateTime.now())
                .build();

        // IMPORTANTE: Feedback será enviado ao GLPI via GlpiClient
        // Não salvamos localmente (apenas Redis/Caffeine para cache temporário)
        log.info("Feedback preparado para envio ao GLPI - Ticket #{}", ticketId);

        return feedback;
    }

    /**
     * Monta mensagem de solicitação de feedback
     */
    public String buildFeedbackRequestMessage(Long ticketId, String ticketTitle) {
        return String.format("""
                ⭐ *Como foi o atendimento?*

                Seu chamado *#%d* foi finalizado:
                📋 %s

                Por favor, avalie nosso atendimento de *1 a 5 estrelas*:

                1️⃣ = Muito ruim
                2️⃣ = Ruim
                3️⃣ = Regular
                4️⃣ = Bom
                5️⃣ = Excelente

                Digite apenas o número da sua avaliação.
                """,
                ticketId, ticketTitle);
    }

    /**
     * Monta mensagem de agradecimento após feedback
     */
    public String buildFeedbackThankYouMessage(int rating, Optional<String> comment) {
        String stars = "⭐".repeat(rating);

        String message = String.format("""
                🎉 *Obrigado pela sua avaliação!*

                Você avaliou nosso atendimento com: %s (%d/5)
                """, stars, rating);

        if (comment.isPresent() && !comment.get().isBlank()) {
            message += String.format("""

                    💬 *Seu comentário:*
                    "%s"
                    """, comment.get());
        }

        message += """

                Sua opinião é muito importante para melhorarmos continuamente!

                💬 *Precisa de ajuda novamente?*
                Digite *oi* para abrir um novo chamado

                📞 *Suporte:* Ramal *3018*
                """;

        return message;
    }

    /**
     * Monta mensagem explicativa para avaliação inválida
     */
    public String buildInvalidRatingMessage() {
        return """
                ⚠️ *Avaliação inválida*

                Por favor, digite apenas um número de *1 a 5*:

                1️⃣ = Muito ruim
                2️⃣ = Ruim
                3️⃣ = Regular
                4️⃣ = Bom
                5️⃣ = Excelente

                📞 *Prefere não avaliar?* Digite */cancelar*
                    ou ligue no ramal *3018*
                """;
    }

    /**
     * Analisa sentimento do comentário (simples por palavras-chave)
     * Pode ser melhorado com NLP/ML no futuro
     */
    public String analyzeSentiment(String comment) {
        if (comment == null || comment.isBlank()) {
            return "NEUTRAL";
        }

        String lowerComment = comment.toLowerCase();

        // Palavras positivas
        String[] positiveWords = {"ótimo", "excelente", "bom", "rápido", "eficiente",
                "satisfeito", "perfeito", "maravilhoso", "obrigado", "parabéns"};
        // Palavras negativas
        String[] negativeWords = {"ruim", "péssimo", "lento", "demorado", "insatisfeito",
                "problema", "falha", "erro", "horrível", "decepcionado"};

        long positiveCount = 0;
        long negativeCount = 0;

        for (String word : positiveWords) {
            if (lowerComment.contains(word)) positiveCount++;
        }

        for (String word : negativeWords) {
            if (lowerComment.contains(word)) negativeCount++;
        }

        if (positiveCount > negativeCount) return "POSITIVE";
        if (negativeCount > positiveCount) return "NEGATIVE";
        return "NEUTRAL";
    }

    /**
     * REMOVIDO: Métricas de feedback são consultadas diretamente no GLPI.
     * Não mantemos persistência local - apenas cache temporário (Redis/Caffeine).
     */
}
