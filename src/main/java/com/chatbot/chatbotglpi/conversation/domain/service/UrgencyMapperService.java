package com.chatbot.chatbotglpi.conversation.domain.service;

import com.chatbot.chatbotglpi.conversation.application.port.input.UrgencyMapperPort;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Domain Service para mapeamento de urgências.
 * Contém lógica de domínio para níveis de urgência.
 */
@Service
public class UrgencyMapperService implements UrgencyMapperPort {

    private static final Map<String, String> URGENCY_MAP = new LinkedHashMap<>();

    static {
        URGENCY_MAP.put("1", "Baixa");
        URGENCY_MAP.put("baixa", "Baixa");
        URGENCY_MAP.put("2", "Média");
        URGENCY_MAP.put("media", "Média");
        URGENCY_MAP.put("média", "Média");
        URGENCY_MAP.put("3", "Alta");
        URGENCY_MAP.put("alta", "Alta");
        URGENCY_MAP.put("4", "Crítica");
        URGENCY_MAP.put("critica", "Crítica");
        URGENCY_MAP.put("crítica", "Crítica");
    }

    @Override
    public Optional<String> map(String input) {
        if (input == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(URGENCY_MAP.get(input.trim().toLowerCase()));
    }

    @Override
    public String getAvailableOptions() {
        return "1️⃣ Baixa\n2️⃣ Média\n3️⃣ Alta\n4️⃣ Crítica";
    }
}
