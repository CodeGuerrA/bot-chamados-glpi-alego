package com.chatbot.chatbotglpi.conversation.domain.service;

import com.chatbot.chatbotglpi.conversation.application.port.input.TitleGeneratorPort;
import com.chatbot.chatbotglpi.shared.util.TituloNaturalPTBR;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Domain Service para geração de títulos usando NLP.
 * Contém lógica de domínio para criação automática de títulos.
 */
@Service
@RequiredArgsConstructor
public class NlpTitleGeneratorService implements TitleGeneratorPort {
    private final TituloNaturalPTBR tituloNaturalPTBR;

    @Override
    public String generateTitle(String description) {
        return tituloNaturalPTBR.gerarTitulo(description);
    }
}
