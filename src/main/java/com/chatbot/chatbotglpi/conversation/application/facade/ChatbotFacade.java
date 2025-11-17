package com.chatbot.chatbotglpi.conversation.application.facade;

public interface ChatbotFacade {
    String processMessage(String phone, String message);
    String cancelConversation(String phone);

}
