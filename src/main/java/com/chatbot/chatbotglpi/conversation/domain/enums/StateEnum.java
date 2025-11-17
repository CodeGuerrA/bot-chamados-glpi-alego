package com.chatbot.chatbotglpi.conversation.domain.enums;


/**
 * Estados possíveis de uma conversa com o bot.
 */
public enum StateEnum {
    GREETING,
    COLLECTING_DESCRIPTION,
    COLLECTING_CATEGORY,
    COLLECTING_URGENCY,
    CONFIRMING,
    COMPLETED,
    COLLECTING_LOCATION,
    COLLECTING_RAMAL
}

