package com.chatbot.chatbotglpi.conversation.domain.state;

import com.chatbot.chatbotglpi.conversation.application.port.input.SummaryBuilderPort;
import com.chatbot.chatbotglpi.conversation.application.port.input.UpdateSummaryBuilderPort;
import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import com.chatbot.chatbotglpi.conversation.domain.enums.StateEnum;
import com.chatbot.chatbotglpi.conversation.domain.validator.base.Validator;
import com.chatbot.chatbotglpi.conversation.domain.validator.ramal.RamalValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CollectingRamalState implements ChatState {

    private final SummaryBuilderPort summaryBuilderPort;
    private final UpdateSummaryBuilderPort updateSummaryBuilderPort;
    private final RamalValidator ramalValidator;

    @Override
    public String handleMessage(ConversationState state, String message) {
        // Comando de ajuda contextual
        if (message.trim().equalsIgnoreCase("/ajuda") || message.trim().equalsIgnoreCase("ajuda")) {
            return getHelpMessage();
        }

        // 1. Valida o ramal
        Validator.ValidationResult validationResult = ramalValidator.validate(message);
        if (!validationResult.isValid()) {
            return validationResult.errorMessage() + "\n\n" +
                   "💡 *Dica:* Digite */ajuda* para ver exemplos de ramal.";
        }

        // 2. Salva o ramal
        state.addData("ramal", message);

        if (handleReturnAfterEdit(state)) {
            return "✅ Ramal atualizado com sucesso!\n\n" +
                   "Voltando para confirmação do chamado...\n\n" +
                   updateSummaryBuilderPort.build(state);
        }

        // 3. Avança para estado de confirmação
        state.setCurrentState(StateEnum.CONFIRMING);

        // 4. Retorna resumo para confirmação
        return String.format("""
                ✅ Ramal registrado!

                %s

                *Confirma a criação do chamado?*

                ✅ Digite *SIM* para confirmar
                ❌ Digite *NÃO* para cancelar
                ✏️ Digite *1*, *2*, *3* ou *4* para editar um campo

                💡 Digite */ajuda* se precisar de orientação
                """, summaryBuilderPort.build(state));
    }

    private String getHelpMessage() {
        return "❓ *AJUDA - Ramal*\n\n" +
               "*Formato correto:*\n" +
               "→ Apenas números\n" +
               "→ Entre 3 e 6 dígitos\n" +
               "→ Exemplos: ```1234```, ```567```, ```12345```\n\n" +
               "*Formatos inválidos:*\n" +
               "→ ```123-456``` (com traço)\n" +
               "→ ```(1234)``` (com parênteses)\n" +
               "→ ```ramal123``` (com letras)\n" +
               "→ ```12``` (muito curto)\n\n" +
               "Digite apenas os números do ramal";
    }
}
