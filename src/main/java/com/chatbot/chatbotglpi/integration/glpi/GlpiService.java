package com.chatbot.chatbotglpi.integration.glpi;

import com.chatbot.chatbotglpi.conversation.application.port.output.TicketGateway;
import com.chatbot.chatbotglpi.integration.glpi.dto.CreateTicketRequest;
import com.chatbot.chatbotglpi.integration.glpi.dto.CreateTicketResponse;
import com.chatbot.chatbotglpi.integration.glpi.enums.GlpiTicketStatus;
import com.chatbot.chatbotglpi.integration.glpi.enums.GlpiTicketType;
import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GlpiService implements TicketGateway {

    private final GlpiClient glpiClient;
    private final GlpiSearch glpiSearch;
    private final GlpiAttribution glpiAttribution;

    /**
     * Implementação do TicketGateway - cria ticket e retorna ID
     */
    @Override
    public Long createTicket(ConversationState state) throws Exception {
        CreateTicketResponse response = createTicketFromConversation(state);
        return response.getId().longValue();
    }

    /**
     * Cria um ticket no GLPI a partir dos dados da conversa
     * Tipo padrão: Requisição
     * Status padrão: Novo
     */
    public CreateTicketResponse createTicketFromConversation(ConversationState state) throws Exception {
        // Monta o conteúdo da descrição com Local e Ramal
        String descricao = state.getData("description");
        String local = state.getData("locate");
        String ramal = state.getData("ramal");
        String content = String.format("%s (Local: %s, Ramal: %s)", descricao, local, ramal);

        // Título gerado automaticamente
        String titulo = state.getData("title");

        // Monta o objeto CreateTicketRequest
        CreateTicketRequest ticketRequest = CreateTicketRequest.builder()
                .name(titulo)   // título gerado pelo TituloNaturalPTBR
                .content(content) // descrição completa com local e ramal
                .type(GlpiTicketType.REQUISICAO.getCode())
                .status(GlpiTicketStatus.NOVO.getCode())
                .build();

        // Cria ticket via cliente GLPI
        CreateTicketResponse ticketResponse = glpiClient.createTicket(ticketRequest);
        Integer ticketID = ticketResponse.getId();

        // Atribui o ticket ao usuário se encontrado
        Integer userId = glpiSearch.getUserIdByLogin(state.getData("username"));
        if (userId != null) {
            glpiAttribution.assignTicketToUser(ticketID, userId);
        }

        log.info("Chamado criado no GLPI: {} (ID: {})", titulo, ticketID);

        // Retorna o ticket criado
        return ticketResponse;
    }
}
