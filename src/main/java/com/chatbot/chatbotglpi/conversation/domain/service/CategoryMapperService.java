package com.chatbot.chatbotglpi.conversation.domain.service;

import com.chatbot.chatbotglpi.conversation.application.port.input.CategoryMapperPort;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Domain Service para mapeamento de categorias.
 * Contém lógica de domínio para categorização de tickets.
 */
@Service
public class CategoryMapperService implements CategoryMapperPort {

    private static final Map<String, String> CATEGORY_MAP = new LinkedHashMap<>();

    static {
        CATEGORY_MAP.put("1", "Hardware");
        CATEGORY_MAP.put("hardware", "Hardware");
        CATEGORY_MAP.put("2", "Software");
        CATEGORY_MAP.put("software", "Software");
        CATEGORY_MAP.put("3", "Rede");
        CATEGORY_MAP.put("rede", "Rede");
        CATEGORY_MAP.put("4", "Acesso");
        CATEGORY_MAP.put("acesso", "Acesso");
    }

    @Override
    public Optional<String> map(String input) {
        if (input == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(CATEGORY_MAP.get(input.trim().toLowerCase()));
    }

    @Override
    public String getAvailableOptions() {
        return "1️⃣ Hardware\n2️⃣ Software\n3️⃣ Rede\n4️⃣ Acesso";
    }
}
