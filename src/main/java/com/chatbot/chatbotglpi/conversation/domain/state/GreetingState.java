package com.chatbot.chatbotglpi.conversation.domain.state;

import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import com.chatbot.chatbotglpi.conversation.domain.enums.StateEnum;
import com.chatbot.chatbotglpi.conversation.infrastrcture.metrics.BotMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Estado de saudação inicial.
 * SRP - única responsabilidade de cumprimentar e iniciar coleta.
 */
@Component
@RequiredArgsConstructor
public class GreetingState implements ChatState {

    private final BotMetrics botMetrics;

    @Override
    public String handleMessage(ConversationState state, String message) {
        // Registra métrica de conversa iniciada
        botMetrics.recordConversationStarted();

        state.setCurrentState(StateEnum.COLLECTING_USERNAME);


        return """
                👋 Olá! Sou o *Bot de Suporte da ALEGO*

                Vou te ajudar a abrir um chamado de forma rápida e prática.

                *Qual é o seu usuário de rede?*

                Digite o mesmo usuário que você usa para acessar os sistemas da ALEGO
                (ex: ```nome.sobrenome``` ou ```nome.sobrenome2```)

                💡 Digite */ajuda* se tiver dúvidas
                """;

    }
}
