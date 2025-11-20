package com.chatbot.chatbotglpi.conversation.application.usecase;

import com.chatbot.chatbotglpi.conversation.application.port.output.ConversationStateRepository;
import com.chatbot.chatbotglpi.conversation.application.port.output.TicketGateway;
import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import com.chatbot.chatbotglpi.conversation.domain.enums.StateEnum;
import com.chatbot.chatbotglpi.conversation.infrastrcture.metrics.BotMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Use Case para criação de tickets.
 * Operação única: criar ticket no sistema externo.
 * SRP - Single Responsibility Principle.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateTicketUseCase {

    private final TicketGateway ticketGateway;
    private final ConversationStateRepository conversationRepository;
    private final BotMetrics botMetrics;

    public String execute(ConversationState state) {
        log.info("Criando ticket para {}", state.getPhone());

        try {
            if (!state.isComplete()) {
                log.warn("Dados incompletos para criação de ticket: {}", state.getPhone());
                return "Não foi possível criar o chamado. Dados incompletos. Digite *oi* para começar novamente.";
            }
            // Cria ticket via gateway (agora usando abstração)
            Long ticketId = ticketGateway.createTicket(state);

            // Registra métrica de conversa completada
            botMetrics.recordConversationCompleted();

            // TODO: Registrar duração da conversa quando adicionar campo createdAt em ConversationState
            // if (state.getCreatedAt() != null) {
            //     botMetrics.recordConversationDuration(state.getCreatedAt(), LocalDateTime.now());
            // }

            // Marca conversa como completa
            state.setCurrentState(StateEnum.COMPLETED);

            // Remove conversa do repositório
            conversationRepository.delete(state.getPhone());

            return buildSuccessMessage(ticketId, state);

        } catch (Exception e) {
            log.error("Erro ao criar ticket para {}: ", state.getPhone(), e);
            return "Erro ao criar o chamado. Digite *oi* para tentar novamente.";
        }
    }

    //arrumar isso daqui com a interface build que tenho
    private String buildSuccessMessage(Long ticketId, ConversationState state) {
        return String.format("""
                        ✅ *Chamado criado com sucesso!*
                        
                        📋 *Número do Chamado:* #%d
                        
                        📌 *Título:*
                           %s
                        
                        📝 *Descrição:*
                           %s
                        
                        📍 *Localização:*
                           %s
                        
                        📞 *Ramal de contato:*
                           %s
                        
                        ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                        
                        ⏳ *Status:* Em análise
                        
                        👨‍💻 *Próximos passos:*
                         Um técnico irá ao local para resolver o chamado.
                        
                        🔔 Você receberá notificações sobre qualquer atualização do chamado.
                        
                        ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                        
                        💬 Para abrir um novo chamado, digite *oi*
                        📞 Em caso de dúvidas, ligue para *3018*
                        """,
                ticketId,
                state.getData("title"),
                state.getData("description"),
                state.getData("locate"),
                state.getData("ramal")
        );

    }
}
