package com.chatbot.chatbotglpi.conversation.domain.helper;

import com.chatbot.chatbotglpi.conversation.domain.enums.StateEnum;

import java.util.Map;

public final class StateNavigationHelper {

    // Mapeamento imutável entre estado atual e anterior
    private static final Map<StateEnum, StateEnum> PREVIOUS_STATE_MAP = Map.of(
            StateEnum.COLLECTING_LOCATION, StateEnum.COLLECTING_DESCRIPTION,
            StateEnum.COLLECTING_RAMAL, StateEnum.COLLECTING_LOCATION,
            StateEnum.CONFIRMING, StateEnum.COLLECTING_RAMAL
//            StateEnum.COLLECTING_CATEGORY, StateEnum.COLLECTING_DESCRIPTION,
//            StateEnum.COLLECTING_URGENCY, StateEnum.COLLECTING_CATEGORY,
//            StateEnum.CONFIRMING, StateEnum.COLLECTING_URGENCY
    );

    // Construtor privado para impedir instância
    private StateNavigationHelper() {}

    /**
     * Retorna o estado anterior baseado no fluxo da conversa.
     *
     * @param currentState estado atual
     * @return estado anterior, ou o mesmo caso não exista anterior válido
     */
    public static StateEnum getPrevious(StateEnum currentState) {
        return PREVIOUS_STATE_MAP.getOrDefault(currentState, currentState);
    }
}
