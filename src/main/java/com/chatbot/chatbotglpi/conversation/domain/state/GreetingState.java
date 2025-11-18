package com.chatbot.chatbotglpi.conversation.domain.state;

import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import com.chatbot.chatbotglpi.conversation.domain.enums.StateEnum;
import org.springframework.stereotype.Component;

/**
 * Estado de saudação inicial.
 * SRP - única responsabilidade de cumprimentar e iniciar coleta.
 */
@Component
public class GreetingState implements ChatState {

    @Override
    public String handleMessage(ConversationState state, String message) {
        state.setCurrentState(StateEnum.COLLECTING_USERNAME);


        return """
                👋 Olá! Eu sou o *Bot de Suporte da ALEGO*.
                
                Antes de começarmos, preciso de uma informação para te ajudar.
                
                👉 *Qual é o seu usuário (username)?*
                
                📝 Por favor, digite seu usuário exatamente como você usa para entrar nos sistema da Alego.
                
                Geralmente o seu usuário tem nome, sobrenome e um número no final.
                
                Exemplos:
                ✅ ```nome.sobrenome1```
                ✅ ```carlos.garcia2```
                """;

    }
}
