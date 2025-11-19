package com.chatbot.chatbotglpi.conversation.domain.state;

import com.chatbot.chatbotglpi.conversation.application.port.input.UpdateSummaryBuilderPort;
import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import com.chatbot.chatbotglpi.conversation.domain.enums.StateEnum;
import com.chatbot.chatbotglpi.conversation.domain.validator.base.Validator;
import com.chatbot.chatbotglpi.conversation.domain.validator.locate.LocateValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CollectingLocationState implements ChatState {
    private final UpdateSummaryBuilderPort updateSummaryBuilderPort;
    private final LocateValidator locateValidator;


    @Override
    public String handleMessage(ConversationState state, String message) {
        // Comando de ajuda contextual
        if (message.trim().equalsIgnoreCase("/ajuda") || message.trim().equalsIgnoreCase("ajuda")) {
            return getHelpMessage();
        }

        Validator.ValidationResult validationResult = locateValidator.validate(message);

        if (!validationResult.isValid()) {
            return validationResult.errorMessage() + "\n\n" +
                    "💡 *Dica:* Digite */ajuda* para ver exemplos de locais.";
        }
        state.addData("locate", message);
        if (handleReturnAfterEdit(state)) {
            return "✅ Local atualizado com sucesso!\n\n" +
                    "Voltando para confirmação do chamado...\n\n" +
                    updateSummaryBuilderPort.build(state);
        }

        state.setCurrentState(StateEnum.COLLECTING_RAMAL);
        String currentRamal = state.getData("ramal");
        return """
                ✅ Local registrado com sucesso!
                
                *Qual é o seu ramal para contato?*
                
                %s
                """.formatted(
                currentRamal != null
                        ? "📞 Ramal atual: " + currentRamal + "\n\nSe quiser alterar, é só informar o novo ramal. 😊"
                        : "Por favor, digite apenas os números do ramal (entre 3 e 6 dígitos).\n\n💡 Digite */ajuda* se precisar de exemplos."
        );

    }

    private String getHelpMessage() {
        return "❓ *AJUDA - Local*\n\n" +
                "*Bons exemplos:*\n" +
                "→ ```Sala 101```\n" +
                "→ ```Gabinete Deputado João```\n" +
                "→ ```Recepção Principal```\n" +
                "→ ```Setor TI```\n" +
                "→ ```Auditório```\n\n" +
                "*Evite:*\n" +
                "→ Locais genéricos: ```aqui```\n\n" +
                "Seja específico para facilitar o atendimento!";
    }
}
