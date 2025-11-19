package com.chatbot.chatbotglpi.conversation.domain.state;

import com.chatbot.chatbotglpi.conversation.application.port.input.UpdateSummaryBuilderPort;
import com.chatbot.chatbotglpi.conversation.domain.validator.base.Validator;
import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import com.chatbot.chatbotglpi.conversation.domain.enums.StateEnum;
import com.chatbot.chatbotglpi.conversation.domain.validator.username.UsernameValidator;
import com.chatbot.chatbotglpi.integration.glpi.GlpiSearch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CollectionUsernameState implements ChatState {
    private final UpdateSummaryBuilderPort updateSummaryBuilderPort;
    private final UsernameValidator usernameValidator;
    private final GlpiSearch glpiSearch;

    @Override
    public String handleMessage(ConversationState state, String message) {
        // Comando de ajuda contextual
        if (message.trim().equalsIgnoreCase("/ajuda") || message.trim().equalsIgnoreCase("ajuda")) {
            return getHelpMessage();
        }

        // Valida formato do username
        Validator.ValidationResult validationResult = usernameValidator.validate(message);
        if (!validationResult.isValid()) {
            return validationResult.errorMessage() + "\n\n" +
                    "💡 *Dica:* Digite */ajuda* para ver exemplos de como preencher.";
        }

        // CRÍTICO: Valida se username existe no GLPI
        log.info("Validando username '{}' no GLPI...", message);
        try {
            log.debug("Username recebido para validação: '{}'", message);
            //lembrar disso daqui
            Integer userId = glpiSearch.getUserIdByLogin(message);
            if (userId == null) {
                log.warn("Username '{}' não encontrado no GLPI", message);
                return "❌ *Usuário não encontrado!*\n\n" +
                        "O username *" + message + "* não existe no sistema GLPI.\n\n" +
                        "⚠️ *Possíveis motivos:*\n" +
                        "• Username digitado incorretamente\n" +
                        "• Usuário ainda não cadastrado no GLPI\n" +
                        "• Você pode estar usando um apelido ao invés do username oficial\n\n" +
                        "📞 *O que fazer?*\n" +
                        "• Verifique com o setor de TI qual é seu username correto\n" +
                        "• Digite */cancelar* para sair e tentar mais tarde\n" +
                        "• Digite */ajuda* para ver exemplos de formato correto\n\n" +
                        "Digite seu username novamente:";
            }

            // Salva username E o ID do GLPI para uso posterior
            log.info("Username '{}' validado com sucesso. GLPI User ID: {}", message, userId);
            state.addData("username", message);
            state.addData("glpiUserId", userId.toString());

        } catch (Exception e) {
            log.error("Erro ao validar username no GLPI", e);
            return "⚠️ *Erro temporário ao validar usuário*\n\n" +
                    "Não consegui verificar seu username no momento.\n" +
                    "Isso pode ser uma instabilidade temporária.\n\n" +
                    "Por favor, tente novamente em alguns segundos.\n\n" +
                    "Digite seu username:";
        }

        if (handleReturnAfterEdit(state)) {
            return "✅ Usuário atualizado com sucesso!\n\n" +
                    "Voltando para confirmação do chamado...\n\n" +
                    updateSummaryBuilderPort.build(state);
        }
        state.setCurrentState(StateEnum.COLLECTING_DESCRIPTION);

        String currentDecription = state.getData("description");

        if (currentDecription != null) {
            return """
                    ✅ Usuário registrado com sucesso!
                    
                    📝 *Descrição atual:*
                    %s
                    
                    Se quiser mudar, basta digitar uma nova descrição.
                    Se estiver tudo certo, pode enviar a mesma descrição. 😊
                    """.formatted(currentDecription);

        }

        return """
                ✅ Usuário registrado com sucesso!
                
                *Agora preciso entender qual é o problema.*
                
                Por favor, conte com suas próprias palavras o que está acontecendo.
                Não se preocupe: pode explicar do seu jeito mesmo. 😊
                
                💡 Digite */ajuda* se quiser ver exemplos de como descrever o problema.
                """;
    }

    private String getHelpMessage() {
        return "❓ *AJUDA - Usuário*\n\n" +
                "*Formato correto:*\n" +
                "→ ```nome.sobrenome``` ou ```nome.sobrenome2```\n\n" +
                "*Formatos inválidos:*\n" +
                "→ João Silva (com espaço)\n" +
                "→ joao_silva (com underline)\n" +
                "→ 12345 (apenas números)\n\n" +
                "Digite seu usuário de rede da ALEGO";
    }
}
