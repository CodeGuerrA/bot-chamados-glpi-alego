package com.chatbot.chatbotglpi.conversation.domain.exception;

import com.chatbot.chatbotglpi.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Exceção lançada quando há erros no processamento da conversa.
 *
 * Esta exceção é usada para problemas relacionados ao fluxo conversacional,
 * como estados inválidos, transições não permitidas, ou falhas ao salvar/recuperar
 * o estado da conversa do Redis.
 *
 * Exemplo de uso:
 * if (state == null && !isNewConversation) {
 *     throw new ConversationException("Estado da conversa não encontrado");
 * }
 */
public class ConversationException extends BusinessException {

    private static final String DEFAULT_ERROR_CODE = "error.conversation";

    /**
     * Construtor com mensagem de erro.
     *
     * @param message Descrição do erro na conversa
     */
    public ConversationException(String message) {
        super(DEFAULT_ERROR_CODE, message);
    }

    /**
     * Construtor com código de erro e mensagem.
     *
     * @param errorCode Código do erro para i18n
     * @param message Descrição do erro na conversa
     */
    public ConversationException(String errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * Construtor com mensagem e causa raiz.
     *
     * @param message Descrição do erro
     * @param cause Exceção que originou este erro
     */
    public ConversationException(String message, Throwable cause) {
        super(DEFAULT_ERROR_CODE, message, cause);
    }

    /**
     * Construtor com código de erro, mensagem e causa raiz.
     *
     * @param errorCode Código do erro para i18n
     * @param message Descrição do erro
     * @param cause Exceção que originou este erro
     */
    public ConversationException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.UNPROCESSABLE_ENTITY;
    }
}
