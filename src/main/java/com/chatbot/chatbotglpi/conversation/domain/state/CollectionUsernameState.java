package com.chatbot.chatbotglpi.conversation.domain.state;

import com.chatbot.chatbotglpi.conversation.application.port.input.UpdateSummaryBuilderPort;
import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import com.chatbot.chatbotglpi.conversation.domain.enums.StateEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CollectionUsernameState implements ChatState {
    private final UpdateSummaryBuilderPort updateSummaryBuilderPort;

    @Override
    public String handleMessage(ConversationState state, String message) {

        state.addData("username", message);

        if (handleReturnAfterEdit(state)) {
            return "Usuario atualizado. Voltando para confirmação de chamado. \n " + updateSummaryBuilderPort;
        }
        state.setCurrentState(StateEnum.COLLECTING_DESCRIPTION);

        String currentDecription = state.getData("description");

        // Caso já exista descrição, arrumar isso daqui nos outros collections.
        if (currentDecription != null) {
            return "👍 Usuário registrado!\n\n" +
                    "Descrição atual do chamado:\n" +
                    "➡️ " + currentDecription + "\n\n" +
                    "Se quiser mudar, basta digitar a nova descrição:";
        }

        // Caso não exista
        return "👍 Usuário registrado com sucesso!\n\n" +
                "Agora, por favor, digite uma descrição clara do problema:";

    }
}
