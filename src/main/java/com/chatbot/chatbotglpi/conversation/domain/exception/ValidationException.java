package com.chatbot.chatbotglpi.conversation.domain.exception;

import com.chatbot.chatbotglpi.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Exceção lançada quando há problemas de validação de dados.
 *
 * Esta exceção é usada quando os dados fornecidos pelo usuário não atendem
 * aos requisitos de validação (tamanho, formato, etc).
 *
 * Exemplo de uso:
 * if (title.length() < minLength) {
 *     throw new ValidationException("Título muito curto");
 * }
 */
public class ValidationException extends BusinessException {

    private static final String DEFAULT_ERROR_CODE = "error.validation";

    /**
     * Construtor com mensagem de erro.
     *
     * @param message Descrição do erro de validação
     */
    public ValidationException(String message) {
        super(DEFAULT_ERROR_CODE, message);
    }

    /**
     * Construtor com código de erro e mensagem.
     *
     * @param errorCode Código do erro para i18n
     * @param message Descrição do erro de validação
     */
    public ValidationException(String errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * Construtor com mensagem e causa raiz.
     *
     * @param message Descrição do erro
     * @param cause Exceção que originou este erro
     */
    public ValidationException(String message, Throwable cause) {
        super(DEFAULT_ERROR_CODE, message, cause);
    }

    /**
     * Construtor com código de erro, mensagem e causa raiz.
     *
     * @param errorCode Código do erro para i18n
     * @param message Descrição do erro
     * @param cause Exceção que originou este erro
     */
    public ValidationException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
