package com.chatbot.chatbotglpi.conversation.domain.state;

import com.chatbot.chatbotglpi.conversation.application.port.input.LocateValidatorPort;
import com.chatbot.chatbotglpi.conversation.application.port.input.UpdateSummaryBuilderPort;
import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import com.chatbot.chatbotglpi.conversation.domain.enums.StateEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CollectingLocationState implements ChatState {
    private final LocateValidatorPort locateValidatorPort;
    private final UpdateSummaryBuilderPort updateSummaryBuilderPort;


    @Override
    public String handleMessage(ConversationState state, String message) {
        LocateValidatorPort.ValidationResult validation = locateValidatorPort.validate(message);
        if (!validation.isValid()) {
            return validation.errorMessage();
        }
        state.addData("locate", message);
        if (handleReturnAfterEdit(state)) {
            return "Local atualizada. Voltando para confirmação do chamado.\n " + updateSummaryBuilderPort.build(state);
        }

        state.setCurrentState(StateEnum.COLLECTING_RAMAL);
        String currentRamal = state.getData("ramal");
        return """
                ✅ Ótimo! O local foi registrado com sucesso. 😊
                
                Agora preciso que você me informe o *número do seu ramal* para continuar.
                
                """ + (currentRamal != null
                ? "📝 O ramal informado até agora é: " + currentRamal +
                "\nSe estiver tudo certinho, podemos seguir. Caso queira corrigir, é só digitar o ramal correto."
                : "Por favor, digite o número do seu ramal para continuarmos:");

    }
}
