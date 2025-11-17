package com.chatbot.chatbotglpi.shared.util;

import lombok.experimental.UtilityClass;

import java.util.Set;

/**
 * Classe utilitária para sanitização de inputs do usuário.
 *
 * Esta classe fornece métodos para limpar e validar dados de entrada,
 * prevenindo ataques de injeção (XSS, SQL Injection, etc) e garantindo
 * que os dados estejam em formato adequado para processamento.
 *
 * A anotação @UtilityClass do Lombok torna esta classe final, com construtor
 * privado e todos os métodos estáticos automaticamente.
 *
 * Exemplos de uso:
 * String safeName = InputSanitizer.sanitize(userInput);
 * String cleanPhone = InputSanitizer.cleanPhone(phoneNumber);
 */
@UtilityClass
public class InputSanitizer {

    /**
     * Caracteres HTML perigosos que podem ser usados em ataques XSS.
     * Estes serão escapados ou removidos durante a sanitização.
     */
    private static final String[] HTML_DANGEROUS_CHARS = {
            "<", ">", "\"", "'", "&", "/", "\\",
            "<script", "</script", "javascript:", "onerror=", "onload="
    };

    /**
     * Sanitiza texto genérico removendo caracteres perigosos.
     *
     * Este método:
     * 1. Remove espaços em branco extras no início e fim
     * 2. Escapa caracteres HTML perigosos
     * 3. Remove scripts e código JavaScript
     * 4. Normaliza espaços múltiplos
     *
     * @param input Texto a ser sanitizado
     * @return Texto limpo e seguro, ou null se input for null
     */
    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }

        // Remove espaços em branco no início e fim
        String sanitized = input.trim();

        // Escapa caracteres HTML perigosos
        sanitized = escapeHtml(sanitized);

        // Remove múltiplos espaços consecutivos, deixando apenas um
        sanitized = sanitized.replaceAll("\\s+", " ");

        // Remove caracteres de controle não imprimíveis (exceto quebras de linha e tabs)
        sanitized = sanitized.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");

        return sanitized;
    }

    /**
     * Escapa caracteres HTML especiais para prevenir XSS.
     *
     * Substitui caracteres perigosos por suas entidades HTML equivalentes:
     * < vira &lt;
     * > vira &gt;
     * " vira &quot;
     * ' vira &#x27;
     * & vira &amp;
     *
     * @param input Texto a ser escapado
     * @return Texto com caracteres HTML escapados
     */
    public static String escapeHtml(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        return input
                .replace("&", "&amp;")   // Deve ser o primeiro para não interferir nos outros
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;")
                .replace("/", "&#x2F;");
    }

    /**
     * Limpa número de telefone removendo caracteres não numéricos
     * e sufixos específicos do WhatsApp.
     *
     * Este método:
     * 1. Remove o sufixo "@s.whatsapp.net" se presente
     * 2. Remove espaços em branco
     * 3. Remove todos os caracteres que não sejam dígitos
     * 4. Remove prefixo de país duplicado se houver
     *
     * Exemplos:
     * "5511999999999@s.whatsapp.net" -> "5511999999999"
     * "+55 (11) 99999-9999" -> "5511999999999"
     * "11 9 9999-9999" -> "11999999999"
     *
     * @param phone Número de telefone bruto
     * @return Número limpo contendo apenas dígitos
     */
    public static String cleanPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return phone;
        }

        // Remove sufixo do WhatsApp
        String cleaned = phone.replace("@s.whatsapp.net", "");

        // Remove espaços, parênteses, hífens, e outros caracteres não numéricos
        cleaned = cleaned.replaceAll("[^0-9]", "");

        // Remove zeros à esquerda (alguns sistemas podem adicionar)
        cleaned = cleaned.replaceFirst("^0+", "");

        return cleaned.trim();
    }

    /**
     * Valida se um telefone tem formato válido após limpeza.
     *
     * Critérios de validação:
     * - Deve conter apenas dígitos
     * - Deve ter entre 10 e 15 dígitos (padrão internacional)
     *
     * @param phone Número de telefone a validar
     * @return true se válido, false caso contrário
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }

        // Limpa o telefone primeiro
        String cleaned = cleanPhone(phone);

        // Verifica se tem apenas dígitos e comprimento adequado
        return cleaned.matches("^[0-9]{10,15}$");
    }

    /**
     * Trunca texto se exceder o tamanho máximo.
     *
     * Útil para garantir que inputs não excedam limites de banco de dados
     * ou de exibição.
     *
     * @param text Texto a ser truncado
     * @param maxLength Comprimento máximo permitido
     * @return Texto truncado se necessário, ou texto original
     */
    public static String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }

        // Trunca e adiciona reticências para indicar que foi cortado
        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * Remove todos os caracteres especiais, mantendo apenas letras,
     * números e espaços.
     *
     * Útil para campos que devem conter apenas texto alfanumérico simples.
     *
     * @param input Texto a ser limpo
     * @return Texto contendo apenas caracteres alfanuméricos e espaços
     */
    public static String removeSpecialChars(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Mantém apenas letras (incluindo acentuadas), números e espaços
        return input.replaceAll("[^\\p{L}\\p{N}\\s]", "");
    }

    /**
     * Valida se o texto contém apenas caracteres seguros.
     *
     * Verifica se não há tentativas de injeção de código HTML ou JavaScript.
     *
     * @param input Texto a ser validado
     * @return true se seguro, false se contém caracteres perigosos
     */
    public static boolean isSafe(String input) {
        if (input == null || input.isEmpty()) {
            return true;
        }

        String lowerInput = input.toLowerCase();

        // Verifica se contém algum padrão perigoso
        for (String dangerous : HTML_DANGEROUS_CHARS) {
            if (lowerInput.contains(dangerous.toLowerCase())) {
                return false;
            }
        }

        return true;
    }
}
