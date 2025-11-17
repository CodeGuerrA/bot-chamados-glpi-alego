package com.chatbot.chatbotglpi.conversation.application.port.input;

import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;

//dps arrumar um caso o seguinte atualizei um campo, em vez de editar outros campos, va para o campo ConfirmingState.
public interface UpdateSummaryBuilderPort {
    String build(ConversationState state);

}
