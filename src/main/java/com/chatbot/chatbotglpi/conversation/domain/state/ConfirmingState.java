package com.chatbot.chatbotglpi.conversation.domain.state;

import com.chatbot.chatbotglpi.conversation.application.port.output.ConversationStateRepository;
import com.chatbot.chatbotglpi.conversation.application.usecase.CreateTicketUseCase;
import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import com.chatbot.chatbotglpi.conversation.domain.enums.StateEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Estado de confirmação do ticket.
 * SRP - única responsabilidade de processar confirmação.
 * DIP - depende de abstrações.
 * OCP - aberto para extensão de campos editáveis.
 */
@Component
@RequiredArgsConstructor
public class ConfirmingState implements ChatState {

    private final ConversationStateRepository conversationRepository;
    private final CreateTicketUseCase createTicketUseCase;

    private static final Map<String, StateEnum> EDITABLE_FIELDS = Map.of(
            "descrição", StateEnum.COLLECTING_DESCRIPTION,
            "descricao", StateEnum.COLLECTING_DESCRIPTION,
            "local", StateEnum.COLLECTING_LOCATION,
            "ramal", StateEnum.COLLECTING_RAMAL
//            "categoria", StateEnum.COLLECTING_CATEGORY,
//            "urgência", StateEnum.COLLECTING_URGENCY,
//            "urgencia", StateEnum.COLLECTING_URGENCY
    );

    private static final Set<String> CONFIRM_OPTIONS = Set.of("sim", "1");
    private static final Set<String> CANCEL_OPTIONS = Set.of("não", "nao", "2");

    @Override
    public String handleMessage(ConversationState state, String message) {
        // 1. Verifica dados completos
        if (!state.isComplete()) {
            return "Dados incompletos. Digite /cancelar e comece novamente.";
        }

        String input = message.trim().toLowerCase();

        // 2. Processa comando de edição (OCP - extensível via mapa)
        if (input.startsWith("voltar")) {
            return handleEditCommand(input, state);
        }

        // 3. Confirma criação
        if (CONFIRM_OPTIONS.contains(input)) {
            return createTicketUseCase.execute(state);
        }

        // 4. Cancela chamado
        if (CANCEL_OPTIONS.contains(input)) {
            conversationRepository.delete(state.getPhone());
            return "Chamado cancelado.\nDigite *oi* para começar novamente.";
        }

        // 5. Entrada inválida
        return "Opção inválida. Digite *SIM* para confirmar, *NÃO* para cancelar ou 'voltar <campo>' para editar.";
    }

    private String handleEditCommand(String input, ConversationState state) {
        String[] parts = input.split(" ", 2);

        if (parts.length < 2) {
            return "Especifique qual campo deseja alterar: descrição, local ou ramal.";
        }

        String field = parts[1].trim();
        StateEnum targetState = EDITABLE_FIELDS.get(field);

        if (targetState == null) {
            return "Campo inválido. Escolha: descrição, local ou ramal.";
        }
        state.setReturnState(StateEnum.CONFIRMING);
//arrumar aqui
        state.setCurrentState(targetState);
        return "Você voltou para o campo *" + field + "*. Por favor, informe novamente.";
    }
}
