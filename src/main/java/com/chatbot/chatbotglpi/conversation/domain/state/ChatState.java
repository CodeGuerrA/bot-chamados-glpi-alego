package com.chatbot.chatbotglpi.conversation.domain.state;

import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;

public interface ChatState {
    String handleMessage(ConversationState state, String message);
    default boolean handleReturnAfterEdit(ConversationState state) {
        if (state.getReturnState() != null) {
            state.setCurrentState(state.getReturnState());
            state.setReturnState(null);
            return true;
        }
        return false;
    }

}
