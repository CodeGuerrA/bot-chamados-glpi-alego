package com.chatbot.chatbotglpi.shared.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Validador de assinatura HMAC-SHA256 para webhooks.
 * Garante autenticidade e integridade das requisições de webhook.
 *
 * Princípios de segurança:
 * - Constant-time comparison (previne timing attacks)
 * - HMAC-SHA256 (criptograficamente seguro)
 * - Validação obrigatória de assinatura
 */
@Slf4j
@Component
public class WebhookSignatureValidator {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    /**
     * Valida assinatura HMAC-SHA256 de um webhook.
     *
     * @param payload Corpo da requisição (JSON string)
     * @param receivedSignature Assinatura recebida no header
     * @param secret Chave secreta compartilhada
     * @return true se assinatura válida, false caso contrário
     */
    public boolean validateSignature(String payload, String receivedSignature, String secret) {
        if (payload == null || receivedSignature == null || secret == null) {
            log.warn("Validação de assinatura falhou: parâmetros nulos");
            return false;
        }

        try {
            // Calcula assinatura esperada
            String expectedSignature = calculateHmacSha256(payload, secret);

            // Constant-time comparison (previne timing attacks)
            boolean isValid = MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    receivedSignature.getBytes(StandardCharsets.UTF_8)
            );

            if (!isValid) {
                log.warn("Assinatura inválida. Esperado: {}, Recebido: {}",
                         expectedSignature.substring(0, 8) + "...",
                         receivedSignature.substring(0, Math.min(8, receivedSignature.length())) + "...");
            }

            return isValid;

        } catch (Exception e) {
            log.error("Erro ao validar assinatura HMAC", e);
            return false;
        }
    }

    /**
     * Calcula HMAC-SHA256 de um payload.
     *
     * @param payload Dados a assinar
     * @param secret Chave secreta
     * @return Assinatura em hexadecimal
     */
    public String calculateHmacSha256(String payload, String secret)
            throws NoSuchAlgorithmException, InvalidKeyException {

        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                HMAC_ALGORITHM
        );
        mac.init(secretKeySpec);

        byte[] hmacBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

        // Converte para hexadecimal
        return HexFormat.of().formatHex(hmacBytes);
    }

    /**
     * Valida assinatura com prefixo "sha256=" (formato GitHub/Evolution API).
     *
     * @param payload Corpo da requisição
     * @param receivedSignature Assinatura com prefixo (ex: "sha256=abc123...")
     * @param secret Chave secreta
     * @return true se válida
     */
    public boolean validateSignatureWithPrefix(String payload, String receivedSignature, String secret) {
        if (receivedSignature == null || !receivedSignature.startsWith("sha256=")) {
            log.warn("Assinatura não possui prefixo sha256=");
            return false;
        }

        String signatureWithoutPrefix = receivedSignature.substring(7); // Remove "sha256="
        return validateSignature(payload, signatureWithoutPrefix, secret);
    }
}
