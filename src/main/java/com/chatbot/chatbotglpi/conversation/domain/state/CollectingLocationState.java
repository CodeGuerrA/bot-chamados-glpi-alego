package com.chatbot.chatbotglpi.conversation.domain.state;

import com.chatbot.chatbotglpi.conversation.application.port.input.LocateValidatorPort;
import com.chatbot.chatbotglpi.conversation.application.port.input.UpdateSummaryBuilderPort;
import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import com.chatbot.chatbotglpi.conversation.domain.enums.StateEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CollectingLocationState implements ChatState{
    private final LocateValidatorPort locateValidatorPort;
    private final UpdateSummaryBuilderPort updateSummaryBuilderPort;


    @Override
    public String handleMessage(ConversationState state, String message) {
        LocateValidatorPort.ValidationResult validation = locateValidatorPort.validate(message);
        if(!validation.isValid()){
            return validation.errorMessage();
        }
        state.addData("locate", message);
        if (handleReturnAfterEdit(state)) {
            return "Local atualizada. Voltando para confirmação do chamado.\n " + updateSummaryBuilderPort.build(state);
        }

        state.setCurrentState(StateEnum.COLLECTING_RAMAL);
        String currentRamal = state.getData("ramal");
        return "Local foi registrado!\n"+
                "Por favor, informe o ramal" +
                (currentRamal !=null ? "\nRamal atual: "  + currentRamal : "\n Digite o ramal correto pfv: ");
    }
}
