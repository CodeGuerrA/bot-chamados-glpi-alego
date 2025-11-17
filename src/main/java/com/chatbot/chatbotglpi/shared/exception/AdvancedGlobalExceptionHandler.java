package com.chatbot.chatbotglpi.shared.exception;

import com.chatbot.chatbotglpi.shared.dto.AdvancedErrorResponseDTO;
import com.chatbot.chatbotglpi.shared.util.StackTraceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class AdvancedGlobalExceptionHandler {

    private final MessageSource messageSource;

    @Value("${app.error.include-stacktrace:false}")
    private boolean includeStackTrace;


    // ------------------------------
    // Validação DTOs e restrições 400
    // ------------------------------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AdvancedErrorResponseDTO> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {

        List<String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        List<String> userMessages = new java.util.ArrayList<>();
        userMessages.add(getMessage("error.validation.detail"));
        userMessages.addAll(fieldErrors);

        AdvancedErrorResponseDTO errorResponse = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                getMessage("error.validation"),
                userMessages,
                request,
                ex
        );

        log.warn("Validation error at {}: {}", request.getDescription(false), fieldErrors);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    // ------------------------------
    // Exceções de Negócio (Business Exception)
    // ------------------------------
    /**
     * Handler unificado para todas as exceções de negócio.
     * Utiliza hierarquia de exceções e i18n para mensagens.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<AdvancedErrorResponseDTO> handleBusinessException(
            BusinessException ex, WebRequest request) {

        String i18nMessage = getMessage(ex.getErrorCode());
        String i18nDetail = getMessage(ex.getErrorCode() + ".detail");

        AdvancedErrorResponseDTO errorResponse = buildErrorResponse(
                ex.getHttpStatus(),
                i18nMessage,
                List.of(i18nDetail),
                request,
                ex
        );

        log.warn("{} [{}] at {}: {}\n{}",
                ex.getClass().getSimpleName(),
                ex.getErrorCode(),
                request.getDescription(false),
                ex.getMessage(),
                StackTraceUtil.getFullStackTrace(ex));

        return new ResponseEntity<>(errorResponse, ex.getHttpStatus());
    }

    // ------------------------------
    // IllegalArgumentException 400
    // ------------------------------
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<AdvancedErrorResponseDTO> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        AdvancedErrorResponseDTO errorResponse = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                getMessage("error.badrequest"),
                List.of(getMessage("error.badrequest.detail")),
                request,
                ex
        );

        log.warn("IllegalArgumentException at {}: {}\n{}",
                request.getDescription(false),
                ex.getMessage(),
                StackTraceUtil.getFullStackTrace(ex));
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // ------------------------------
    // AccessDeniedException 403
    // ------------------------------
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<AdvancedErrorResponseDTO> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {

        AdvancedErrorResponseDTO errorResponse = buildErrorResponse(
                HttpStatus.FORBIDDEN,
                getMessage("error.forbidden"),
                List.of(getMessage("error.forbidden.detail")),
                request,
                ex
        );

        log.warn("AccessDeniedException at {}: {}\n{}",
                request.getDescription(false),
                ex.getMessage(),
                StackTraceUtil.getFullStackTrace(ex));
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    // ------------------------------
    // DataIntegrityViolationException 409
    // ------------------------------
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<AdvancedErrorResponseDTO> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, WebRequest request) {

        AdvancedErrorResponseDTO errorResponse = buildErrorResponse(
                HttpStatus.CONFLICT,
                getMessage("error.conflict"),
                List.of(getMessage("error.conflict.detail")),
                request,
                ex
        );

        log.error("Database error at {}: {}\n{}",
                request.getDescription(false),
                ex.getMessage(),
                StackTraceUtil.getFullStackTrace(ex));
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    // ------------------------------
    // Catch-all 500
    // ------------------------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<AdvancedErrorResponseDTO> handleAllException(
            Exception ex, WebRequest request) {

        AdvancedErrorResponseDTO errorResponse = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                getMessage("error.internal"),
                List.of(getMessage("error.internal.detail")),
                request,
                ex
        );

        log.error("Unexpected error at {}: {}\n{}",
                request.getDescription(false),
                ex.getMessage(),
                StackTraceUtil.getFullStackTrace(ex));
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Busca uma mensagem internacionalizada pelo código.
     * Se a mensagem não for encontrada, retorna o próprio código.
     *
     * @param code código da mensagem
     * @return mensagem internacionalizada
     */
    private String getMessage(String code) {
        try {
            return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            log.warn("Mensagem i18n não encontrada para código: {}", code);
            return code;
        }
    }

    /**
     * Constrói a resposta de erro padronizada.
     *
     * @param status status HTTP
     * @param error título do erro
     * @param userMessages mensagens detalhadas
     * @param request requisição web
     * @return resposta de erro formatada
     */
    private AdvancedErrorResponseDTO buildErrorResponse(
            HttpStatus status,
            String error,
            List<String> userMessages,
            WebRequest request,
            Throwable ex
    ) {
        AdvancedErrorResponseDTO response = new AdvancedErrorResponseDTO(
                LocalDateTime.now(),
                status.value(),
                error,
                userMessages,
                request.getDescription(false).replace("uri=", "")
        );

        if (includeStackTrace && ex != null) {
            AdvancedErrorResponseDTO.DebugInfo debugInfo = new AdvancedErrorResponseDTO.DebugInfo(
                    ex.getClass().getName(),
                    StackTraceUtil.sanitizeMessage(ex.getMessage()),
                    StackTraceUtil.sanitizeStackTrace(ex)
            );
            response.setDebugInfo(debugInfo);
        }

        return response;
    }
}
