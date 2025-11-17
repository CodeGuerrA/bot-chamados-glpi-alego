package com.chatbot.chatbotglpi.conversation.domain.state;

import com.chatbot.chatbotglpi.conversation.application.port.input.RamalValidatorPort;
import com.chatbot.chatbotglpi.conversation.application.port.input.SummaryBuilderPort;
import com.chatbot.chatbotglpi.conversation.application.port.input.UpdateSummaryBuilderPort;
import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import com.chatbot.chatbotglpi.conversation.domain.enums.StateEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CollectingRamalState implements ChatState {

    private final RamalValidatorPort ramalValidatorPort;
    private final SummaryBuilderPort summaryBuilderPort;
    private final UpdateSummaryBuilderPort updateSummaryBuilderPort;

    @Override
    public String handleMessage(ConversationState state, String message) {
        // 1. Valida o ramal
        RamalValidatorPort.ValidationResult validation = ramalValidatorPort.validate(message);
        if (!validation.isValid()) {
            return validation.errorMessage();
        }

        // 2. Salva o ramal
        state.addData("ramal", message);

        if (handleReturnAfterEdit(state)) {
            return "Ramal atualizada. Voltando para confirmação do chamado.\n " + updateSummaryBuilderPort.build(state);
        }

        // 3. Avança para estado de confirmação
        state.setCurrentState(StateEnum.CONFIRMING);

        // 4. Retorna resumo para confirmação
        return "Ramal registrado!\n" + summaryBuilderPort.build(state);
    }
}
