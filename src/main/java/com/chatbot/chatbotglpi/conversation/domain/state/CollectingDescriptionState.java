package com.chatbot.chatbotglpi.conversation.domain.state;

//import com.chatbot.chatbotglpi.conversation.application.port.input.CategoryMapperPort;

import com.chatbot.chatbotglpi.conversation.application.port.input.DescriptionValidatorPort;
import com.chatbot.chatbotglpi.conversation.application.port.input.TitleGeneratorPort;
import com.chatbot.chatbotglpi.conversation.application.port.input.UpdateSummaryBuilderPort;
import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import com.chatbot.chatbotglpi.conversation.domain.enums.StateEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Estado de coleta de descrição.
 * SRP - única responsabilidade de coletar e validar descrição.
 * DIP - depende de abstrações (ports).
 */
@Component
@RequiredArgsConstructor
public class CollectingDescriptionState implements ChatState {

    private final DescriptionValidatorPort descriptionValidator;
    private final TitleGeneratorPort titleGenerator;
    private final UpdateSummaryBuilderPort updateSummaryBuilderPort;

//    private final CategoryMapperPort categoryMapper;

    @Override
    public String handleMessage(ConversationState state, String message) {
        // 1. Valida descrição (DIP - via abstração)
        DescriptionValidatorPort.ValidationResult validation = descriptionValidator.validate(message);
        if (!validation.isValid()) {
            return validation.errorMessage();
        }

        // 2. Salva descrição
        state.addData("description", message);

        // 3. Gera título automaticamente (DIP - via abstração)
        String titulo = titleGenerator.generateTitle(message);
        state.addData("title", titulo);

        if (handleReturnAfterEdit(state)) {
            return "Descrição atualizada. Voltando para confirmação do chamado. \n " + updateSummaryBuilderPort.build(state);
        }

        // 4. Avança para próximo estado
        state.setCurrentState(StateEnum.COLLECTING_LOCATION);

        // 5. Monta resposta
        String currentLocate = state.getData("locate");
//        String categoryOptions = categoryMapper.getAvailableOptions();

        return "Descrição registrada!\n" +
                "Por favor informe o local exato onde o problema está ocorrendo?(sala,gabinete)\n" +
//                categoryOptions +
                (currentLocate != null ? "\nLocal atual: " + currentLocate : "\nDigite o local correto: ");
    }
}
