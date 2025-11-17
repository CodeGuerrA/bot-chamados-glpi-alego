package com.chatbot.chatbotglpi.util;

import com.chatbot.chatbotglpi.shared.util.InputSanitizer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe InputSanitizer.
 *
 * Esta classe testa todas as funções de sanitização e validação de inputs,
 * garantindo que:
 * - Caracteres perigosos são removidos/escapados corretamente
 * - Telefones são limpos e validados adequadamente
 * - Textos são truncados conforme esperado
 * - Validações de segurança funcionam corretamente
 */
@DisplayName("Testes do InputSanitizer")
class InputSanitizerTest {

    // =========================================================================
    // TESTES DO MÉTODO sanitize()
    // =========================================================================

    @Test
    @DisplayName("Deve sanitizar texto removendo espaços extras")
    void testSanitize_RemovesExtraSpaces() {
        String input = "  texto   com   espaços   extras  ";
        String result = InputSanitizer.sanitize(input);

        assertEquals("texto com espaços extras", result);
    }

    @Test
    @DisplayName("Deve escapar caracteres HTML perigosos")
    void testSanitize_EscapesHtmlCharacters() {
        String input = "<script>alert('XSS')</script>";
        String result = InputSanitizer.sanitize(input);

        // Verifica que caracteres < e > foram escapados
        assertTrue(result.contains("&lt;"));
        assertTrue(result.contains("&gt;"));
        assertFalse(result.contains("<script>"));
    }

    @Test
    @DisplayName("Deve retornar null quando input é null")
    void testSanitize_ReturnsNullForNullInput() {
        String result = InputSanitizer.sanitize(null);
        assertNull(result);
    }

    // =========================================================================
    // TESTES DO MÉTODO cleanPhone()
    // =========================================================================

    @Test
    @DisplayName("Deve remover sufixo do WhatsApp do telefone")
    void testCleanPhone_RemovesWhatsAppSuffix() {
        String phone = "5511999999999@s.whatsapp.net";
        String result = InputSanitizer.cleanPhone(phone);

        assertEquals("5511999999999", result);
    }

    @Test
    @DisplayName("Deve remover caracteres não numéricos do telefone")
    void testCleanPhone_RemovesNonNumericCharacters() {
        String phone = "+55 (11) 99999-9999";
        String result = InputSanitizer.cleanPhone(phone);

        assertEquals("5511999999999", result);
    }

    @Test
    @DisplayName("Deve retornar telefone limpo quando já está em formato correto")
    void testCleanPhone_ReturnsCleanPhoneWhenAlreadyClean() {
        String phone = "5511999999999";
        String result = InputSanitizer.cleanPhone(phone);

        assertEquals("5511999999999", result);
    }

    @Test
    @DisplayName("Deve retornar string vazia quando telefone é vazio")
    void testCleanPhone_ReturnsEmptyForEmptyInput() {
        String result = InputSanitizer.cleanPhone("");

        assertEquals("", result);
    }

    // =========================================================================
    // TESTES DO MÉTODO isValidPhone()
    // =========================================================================

    @Test
    @DisplayName("Deve validar telefone válido com 11 dígitos")
    void testIsValidPhone_ReturnsTrueForValidPhone() {
        String phone = "5511999999999";

        assertTrue(InputSanitizer.isValidPhone(phone));
    }

    @Test
    @DisplayName("Deve validar telefone com sufixo WhatsApp")
    void testIsValidPhone_ReturnsTrueForPhoneWithWhatsAppSuffix() {
        String phone = "5511999999999@s.whatsapp.net";

        assertTrue(InputSanitizer.isValidPhone(phone));
    }

    @Test
    @DisplayName("Deve rejeitar telefone muito curto")
    void testIsValidPhone_ReturnsFalseForShortPhone() {
        String phone = "12345";

        assertFalse(InputSanitizer.isValidPhone(phone));
    }

    @Test
    @DisplayName("Deve rejeitar telefone muito longo")
    void testIsValidPhone_ReturnsFalseForLongPhone() {
        String phone = "12345678901234567890";

        assertFalse(InputSanitizer.isValidPhone(phone));
    }

    @Test
    @DisplayName("Deve rejeitar telefone null")
    void testIsValidPhone_ReturnsFalseForNullPhone() {
        assertFalse(InputSanitizer.isValidPhone(null));
    }

    @Test
    @DisplayName("Deve rejeitar telefone vazio")
    void testIsValidPhone_ReturnsFalseForEmptyPhone() {
        assertFalse(InputSanitizer.isValidPhone(""));
    }

    // =========================================================================
    // TESTES DO MÉTODO truncate()
    // =========================================================================

    @Test
    @DisplayName("Deve truncar texto que excede tamanho máximo")
    void testTruncate_TruncatesLongText() {
        String text = "Este é um texto muito longo que precisa ser truncado";
        String result = InputSanitizer.truncate(text, 20);

        // O método trunca em maxLength-3 e adiciona "..." (total = maxLength)
        assertEquals("Este é um texto m...", result);
        assertEquals(20, result.length());
    }

    @Test
    @DisplayName("Deve manter texto que não excede tamanho máximo")
    void testTruncate_KeepsShortText() {
        String text = "Texto curto";
        String result = InputSanitizer.truncate(text, 50);

        assertEquals("Texto curto", result);
    }

    @Test
    @DisplayName("Deve retornar null quando texto é null")
    void testTruncate_ReturnsNullForNullText() {
        String result = InputSanitizer.truncate(null, 10);
        assertNull(result);
    }

    // =========================================================================
    // TESTES DO MÉTODO isSafe()
    // =========================================================================

    @Test
    @DisplayName("Deve identificar texto seguro")
    void testIsSafe_ReturnsTrueForSafeText() {
        String text = "Este é um texto seguro sem código malicioso";

        assertTrue(InputSanitizer.isSafe(text));
    }

    @Test
    @DisplayName("Deve identificar texto com script como inseguro")
    void testIsSafe_ReturnsFalseForScriptTag() {
        String text = "Texto com <script>alert('xss')</script>";

        assertFalse(InputSanitizer.isSafe(text));
    }

    @Test
    @DisplayName("Deve identificar texto com javascript: como inseguro")
    void testIsSafe_ReturnsFalseForJavascriptProtocol() {
        String text = "javascript:alert('xss')";

        assertFalse(InputSanitizer.isSafe(text));
    }

    @Test
    @DisplayName("Deve retornar true para texto null ou vazio")
    void testIsSafe_ReturnsTrueForNullOrEmpty() {
        assertTrue(InputSanitizer.isSafe(null));
        assertTrue(InputSanitizer.isSafe(""));
    }

    // =========================================================================
    // TESTES DO MÉTODO removeSpecialChars()
    // =========================================================================

    @Test
    @DisplayName("Deve remover caracteres especiais mantendo alfanuméricos")
    void testRemoveSpecialChars_RemovesSpecialCharacters() {
        String input = "Texto!@#$%com^&*()caracteres123especiais";
        String result = InputSanitizer.removeSpecialChars(input);

        assertEquals("Textocomcaracteres123especiais", result);
    }

    @Test
    @DisplayName("Deve manter acentos e caracteres Unicode")
    void testRemoveSpecialChars_KeepsAccents() {
        String input = "Texto com açẽntõs e ç";
        String result = InputSanitizer.removeSpecialChars(input);

        assertEquals("Texto com açẽntõs e ç", result);
    }

    @Test
    @DisplayName("Deve manter espaços no texto")
    void testRemoveSpecialChars_KeepsSpaces() {
        String input = "Texto com espaços";
        String result = InputSanitizer.removeSpecialChars(input);

        assertEquals("Texto com espaços", result);
    }
}
