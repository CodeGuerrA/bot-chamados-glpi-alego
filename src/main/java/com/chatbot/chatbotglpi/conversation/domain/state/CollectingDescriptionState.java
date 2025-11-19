package com.chatbot.chatbotglpi.conversation.domain.state;

import com.chatbot.chatbotglpi.conversation.application.port.input.TitleGeneratorPort;
import com.chatbot.chatbotglpi.conversation.application.port.input.UpdateSummaryBuilderPort;
import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import com.chatbot.chatbotglpi.conversation.domain.enums.StateEnum;
import com.chatbot.chatbotglpi.conversation.domain.validator.base.Validator;
import com.chatbot.chatbotglpi.conversation.domain.validator.description.DescriptionValidator;
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

    private final TitleGeneratorPort titleGenerator;
    private final UpdateSummaryBuilderPort updateSummaryBuilderPort;
    private final DescriptionValidator descriptionValidator;
//    private final CategoryMapperPort categoryMapper;

    @Override
    public String handleMessage(ConversationState state, String message) {
        // Comando de ajuda contextual
        if (message.trim().equalsIgnoreCase("/ajuda") || message.trim().equalsIgnoreCase("ajuda")) {
            return getHelpMessage();
        }

        // 1. Valida descrição (DIP - via abstração)
        Validator.ValidationResult validationResult = descriptionValidator.validate(message);
        if (!validationResult.isValid()) {
            return validationResult.errorMessage() + "\n\n" +
                    "💡 *Dica:* Digite */ajuda* para ver exemplos de como descrever.";
        }

        // 2. Salva descrição
        state.addData("description", message);

        // 3. Gera título automaticamente
        String titulo = titleGenerator.generateTitle(message);
        state.addData("title", titulo);
        log.debug("Título gerado automaticamente: {}", titulo);

        if (handleReturnAfterEdit(state)) {
            return "✅ Descrição atualizada com sucesso!\n\n" +
                    "Voltando para confirmação do chamado...\n\n" +
                    updateSummaryBuilderPort.build(state);
        }

        // 4. Avança para próximo estado
        state.setCurrentState(StateEnum.COLLECTING_LOCATION);

        // 5. Monta resposta
        String currentLocate = state.getData("locate");

        return """
                ✅ Descrição registrada com sucesso!
                
                🔍 *Título:* %s
                
                Agora preciso saber *onde o problema está acontecendo*.
                
                %s
                """.formatted(
                titulo,
                currentLocate != null
                        ? "📍 Local atual: " + currentLocate + "\n\nSe quiser mudar, basta informar o novo local. 😊"
                        : "Por favor, informe o local (ex: sala 101, gabinete 5, recepção).\n\n💡 Digite */ajuda* se precisar de exemplos."
        );

    }

    private String getHelpMessage() {
        return "❓ *AJUDA - Descrição*\n\n" +
                "*Bons exemplos:*\n" +
                "→ ```Computador não liga```\n" +
                "→ ```Internet está lenta```\n" +
                "→ ```Impressora travando papel```\n" +
                "→ ```Sistema não abre```\n\n" +
                "*Evite:*\n" +
                "→ Muito curto: ```problema```\n" +
                "→ Muito longo (+ de 500 caracteres)\n\n" +
                "Quanto mais claro, mais rápido resolveremos!";
    }
}
