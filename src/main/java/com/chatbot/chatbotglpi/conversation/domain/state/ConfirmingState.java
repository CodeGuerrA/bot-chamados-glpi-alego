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

    // Mapeamento de campos editáveis por NOME
    private static final Map<String, StateEnum> EDITABLE_FIELDS = Map.of(
            "descrição", StateEnum.COLLECTING_DESCRIPTION,
            "descricao", StateEnum.COLLECTING_DESCRIPTION,
            "local", StateEnum.COLLECTING_LOCATION,
            "ramal", StateEnum.COLLECTING_RAMAL,
            "usuario", StateEnum.COLLECTING_USERNAME,
            "usuário", StateEnum.COLLECTING_USERNAME
    );

    // NOVO: Mapeamento de campos editáveis por NÚMERO (Edição Inline)
    private static final Map<String, StateEnum> EDITABLE_FIELDS_BY_NUMBER = Map.of(
            "1", StateEnum.COLLECTING_USERNAME,
            "2", StateEnum.COLLECTING_DESCRIPTION,
            "3", StateEnum.COLLECTING_LOCATION,
            "4", StateEnum.COLLECTING_RAMAL
    );

    private static final Set<String> CONFIRM_OPTIONS = Set.of("sim", "s");
    private static final Set<String> CANCEL_OPTIONS = Set.of("não", "nao", "n");

    @Override
    public String handleMessage(ConversationState state, String message) {
        // 1. Verifica dados completos
        if (!state.isComplete()) {
            return "Dados incompletos. Digite /cancelar e comece novamente.";
        }

        String input = message.trim().toLowerCase();

        // 2. NOVO: Edição inline por número (1, 2, 3, 4)
        if (EDITABLE_FIELDS_BY_NUMBER.containsKey(input)) {
            StateEnum targetState = EDITABLE_FIELDS_BY_NUMBER.get(input);
            state.setReturnState(StateEnum.CONFIRMING);
            state.setCurrentState(targetState);

            String fieldName = switch (input) {
                case "1" -> "usuário";
                case "2" -> "descrição";
                case "3" -> "local";
                case "4" -> "ramal";
                default -> "campo";
            };

            return "✏️ Você está editando: *" + fieldName + "*\n\n" +
                   "Digite o novo valor para este campo:";
        }

        // 3. Processa comando de edição tradicional (voltar <campo>)
        if (input.startsWith("voltar")) {
            return handleEditCommand(input, state);
        }

        // 4. Comando de ajuda
        if (input.equals("/ajuda") || input.equals("ajuda")) {
            return "❓ *AJUDA - Confirmação*\n\n" +
                   "*Para confirmar:*\n" +
                   "→ Digite *SIM* ou *S*\n\n" +
                   "*Para cancelar:*\n" +
                   "→ Digite *NÃO* ou *N*\n\n" +
                   "*Para editar:*\n" +
                   "→ Digite *1* (usuário), *2* (descrição), *3* (local) ou *4* (ramal)\n\n" +
                   "Ou use: *voltar descrição*, *voltar local*, *voltar ramal*";
        }

        // 5. Confirma criação
        if (CONFIRM_OPTIONS.contains(input)) {
            return createTicketUseCase.execute(state);
        }

        // 6. Cancela chamado
        if (CANCEL_OPTIONS.contains(input)) {
            conversationRepository.delete(state.getPhone());
            return "❌ *Chamado cancelado*\n\n" +
                   "Todos os dados foram removidos.\n\n" +
                   "📞 Precisa de ajuda? Ligue *3018*\n" +
                   "💬 Digite *oi* para abrir novo chamado";
        }

        // 7. Entrada inválida
        return "⚠️ *Opção inválida*\n\n" +
               "Escolha uma opção:\n\n" +
               "✅ *SIM* - Confirmar e criar chamado\n" +
               "❌ *NÃO* - Cancelar tudo\n" +
               "✏️ *1, 2, 3 ou 4* - Editar campo\n\n" +
               "💡 Digite */ajuda* para mais detalhes";
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
