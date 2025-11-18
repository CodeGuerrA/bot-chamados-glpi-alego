package com.chatbot.chatbotglpi.conversation.domain.state;

import com.chatbot.chatbotglpi.conversation.domain.enums.StateEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Factory para estados do chatbot.
 * OCP - aberto para extensão (novos estados), fechado para modificação.
 * DIP - depende de abstrações (ChatState).
 * <p>
 * Usa registro automático via Spring DI.
 */
@Slf4j
@Component
public class StateFactory {
//fazer o completed dps
    private final Map<StateEnum, ChatState> registry;

    /**
     * Construtor com injeção automática de todos os ChatState.
     * Spring injeta automaticamente todos os beans que implementam ChatState.
     */
    public StateFactory(List<ChatState> states) {
        this.registry = new EnumMap<>(StateEnum.class);
        registerStates(states);
    }

    private void registerStates(List<ChatState> states) {
        for (ChatState state : states) {
            StateEnum stateEnum = mapStateToEnum(state);
            if (stateEnum != null) {
                registry.put(stateEnum, state);
                log.debug("Registrado estado: {} -> {}", stateEnum, state.getClass().getSimpleName());
            }
        }

        log.info("StateFactory inicializado com {} estados", registry.size());
        validateRegistration();
    }

    private StateEnum mapStateToEnum(ChatState state) {
        String className = state.getClass().getSimpleName();

        return switch (className) {
            case "GreetingState" -> StateEnum.GREETING;
            case "CollectionUsernameState" -> StateEnum.COLLECTING_USERNAME;
            case "CollectingDescriptionState" -> StateEnum.COLLECTING_DESCRIPTION;
            case "CollectingLocationState" -> StateEnum.COLLECTING_LOCATION;
            case "CollectingRamalState" -> StateEnum.COLLECTING_RAMAL;
//            case "CollectingCategoryState" -> StateEnum.COLLECTING_CATEGORY;
//            case "CollectingUrgencyState" -> StateEnum.COLLECTING_URGENCY;
            case "ConfirmingState" -> StateEnum.CONFIRMING;
            case "CompletedState" -> StateEnum.COMPLETED; // << adicionar aqui
            default -> {
                log.warn("Estado não mapeado: {}", className);
                yield null;
            }
        };
    }

    private void validateRegistration() {
        for (StateEnum stateEnum : StateEnum.values()) {
            if (stateEnum != StateEnum.COMPLETED && !registry.containsKey(stateEnum)) {
                log.error("Estado não registrado: {}", stateEnum);
            }
        }
    }

    public ChatState getState(StateEnum stateEnum) {
        ChatState state = registry.get(stateEnum);

        if (state == null) {
            log.error("Estado não encontrado: {}", stateEnum);
            throw new IllegalArgumentException("Estado não registrado: " + stateEnum);
        }

        return state;
    }
}
