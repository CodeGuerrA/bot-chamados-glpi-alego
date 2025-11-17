package com.chatbot.chatbotglpi.conversation.application.facade;

import com.chatbot.chatbotglpi.conversation.api.dto.request.ValidatedMessage;
import com.chatbot.chatbotglpi.conversation.application.port.input.GlobalCommandHandler;
import com.chatbot.chatbotglpi.conversation.application.port.input.MessageValidationPort;
import com.chatbot.chatbotglpi.conversation.application.port.input.StateProcessorPort;
import com.chatbot.chatbotglpi.conversation.application.port.output.ConversationStateRepository;
import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import com.chatbot.chatbotglpi.conversation.domain.enums.StateEnum;
import com.chatbot.chatbotglpi.conversation.domain.exception.ConversationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Facade que orquestra o fluxo do chatbot.
 * Padrão Facade - simplifica a interface para o cliente (Controller).
 * Orquestra Use Cases, Domain Services e Application Services.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatbotFacadeImpl implements ChatbotFacade {

    private final MessageValidationPort messageValidation;
    private final ConversationStateRepository conversationRepository;
    private final GlobalCommandHandler globalCommandHandler;
    private final StateProcessorPort stateProcessor;

    @Override
    public String processMessage(String phone, String message) {
        log.info("Processando mensagem de {}: {}", phone, message);

        try {
            // 1. Validação de entrada
            ValidatedMessage validatedMessage = messageValidation.validate(phone, message);
            if (!validatedMessage.isValid()) {
                return handleInvalidMessage(validatedMessage);
            }

            // 2. Recupera ou cria estado da conversa
            ConversationState state = getOrCreateConversationState(validatedMessage.getPhone());

            // 3. Atualiza timestamp de atividade
            state.updateActivity();

            // 4. Processa comandos globais
            Optional<String> globalResponse = globalCommandHandler.handle(
                    validatedMessage.getMessage(),
                    state
            );

            if (globalResponse.isPresent()) {
                persistState(validatedMessage.getPhone(), state);
                return globalResponse.get();
            }

            // 5. Processa mensagem no estado atual
            String response = stateProcessor.process(state, validatedMessage.getMessage());

            // 6. Persiste estado se conversa não finalizada
            persistStateIfNotCompleted(validatedMessage.getPhone(), state);

            return response;

        } catch (ConversationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao processar mensagem de {}: ", phone, e);
            throw new ConversationException("Erro ao processar mensagem. Tente novamente.", e);
        }
    }

    @Override
    public String cancelConversation(String phone) {
        log.info("Cancelando conversa para {}", phone);

        try {
            // 1. Valida telefone
            ValidatedMessage validated = messageValidation.validate(phone, "cancel");
            if (!validated.isValid() && validated.getErrorMessage().contains("telefone")) {
                throw new ConversationException(validated.getErrorMessage());
            }

            // 2. Remove conversa do repositório
            conversationRepository.delete(validated.getPhone());

            log.info("Conversa cancelada com sucesso para: {}", validated.getPhone());

            return """
                    Conversa cancelada.

                    Digite *oi* para começar novamente.
                    """;

        } catch (ConversationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao cancelar conversa de {}: ", phone, e);
            throw new ConversationException("Erro ao cancelar conversa", e);
        }
    }

    private String handleInvalidMessage(ValidatedMessage validatedMessage) {
        log.warn("Mensagem inválida: {}", validatedMessage.getErrorMessage());

        if (validatedMessage.getErrorMessage().contains("telefone")) {
            throw new ConversationException(validatedMessage.getErrorMessage());
        }

        return "Mensagem inválida. Por favor, envie uma mensagem válida.";
    }

    private ConversationState getOrCreateConversationState(String phone) {
        return conversationRepository.findByPhone(phone)
                .orElseGet(() -> {
                    log.info("Criando nova conversa para {}", phone);
                    return ConversationState.builder()
                            .phone(phone)
                            .currentState(StateEnum.GREETING)
                            .startedAt(LocalDateTime.now())
                            .build();
                });
    }

    private void persistState(String phone, ConversationState state) {
        conversationRepository.save(phone, state);
    }

    private void persistStateIfNotCompleted(String phone, ConversationState state) {
        if (state.getCurrentState() != StateEnum.COMPLETED) {
            persistState(phone, state);
        }
    }
}
