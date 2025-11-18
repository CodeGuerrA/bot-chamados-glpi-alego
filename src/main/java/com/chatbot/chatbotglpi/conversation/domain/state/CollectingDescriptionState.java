package com.chatbot.chatbotglpi.conversation.domain.state;

//import com.chatbot.chatbotglpi.conversation.application.port.input.CategoryMapperPort;

import com.chatbot.chatbotglpi.conversation.application.port.input.DescriptionValidatorPort;
import com.chatbot.chatbotglpi.conversation.application.port.input.TitleGeneratorPort;
import com.chatbot.chatbotglpi.conversation.application.port.input.UpdateSummaryBuilderPort;
import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import com.chatbot.chatbotglpi.conversation.domain.enums.StateEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Estado de coleta de descrição.
 */
@Slf4j
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
        // A descrição salva aqui é a original do usuário, sem Local/Ramal.
        state.addData("description", message);

        // 3. Gera título automaticamente
        // O título é gerado APENAS pela frase natural (message), sem Local/Ramal.
        String titulo = titleGenerator.generateTitle(message);
        state.addData("title", titulo);
        log.debug("Esse e o titulo nao sei se ta formado: " + titulo);

        if (handleReturnAfterEdit(state)) {
            return "Descrição atualizada. Voltando para confirmação do chamado. \n " + updateSummaryBuilderPort.build(state);
        }

        // 4. Avança para próximo estado
        state.setCurrentState(StateEnum.COLLECTING_LOCATION);

        // 5. Monta resposta
        String currentLocate = state.getData("locate");
//        String categoryOptions = categoryMapper.getAvailableOptions();

        return """
                ✅ Prontinho! Já registrei a descrição do problema. 😊
                
                Agora preciso saber *onde* exatamente o problema está acontecendo.
                Pode ser, por exemplo: sala, gabinete, setor, recepção…
                
                """ + (currentLocate != null
                ? "📝 Local informado até agora: " + currentLocate + "\nSe quiser atualizar, basta enviar o local correto:"
                : "Por favor, me diga o local exato onde o problema está acontecendo:");

    }
}
