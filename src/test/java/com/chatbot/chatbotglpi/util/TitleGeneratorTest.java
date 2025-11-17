package com.chatbot.chatbotglpi.util;

import com.chatbot.chatbotglpi.shared.util.TitleGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe TitleGenerator.
 *
 * <p>Esta classe testa a geração automática de títulos usando NLP (Natural Language Processing),
 * verificando:</p>
 * <ul>
 *   <li>Remoção correta de stopwords</li>
 *   <li>Capitalização adequada de palavras</li>
 *   <li>Tratamento de edge cases (null, vazio, strings especiais)</li>
 *   <li>Validações de segurança</li>
 *   <li>Limitação de tamanho de título</li>
 * </ul>
 *
 * @see TitleGenerator
 */
@DisplayName("Testes do TitleGenerator")
class TitleGeneratorTest {

    // =========================================================================
    // TESTES BÁSICOS - Funcionalidade Principal
    // =========================================================================

    @Test
    @DisplayName("Deve gerar título removendo stopwords e capitalizando")
    void testGerarTituloNLP_BasicFunctionality() {
        String descricao = "o computador não liga";
        String resultado = TitleGenerator.gerarTituloNLP(descricao);

        assertEquals("Computador Não Liga", resultado);
        // Verifica que a stopword "o" foi removida (não deve aparecer como palavra separada)
        assertFalse(resultado.matches(".*\\bO\\b.*")); // Stopword removida
    }

    @Test
    @DisplayName("Deve remover múltiplas stopwords em sequência")
    void testGerarTituloNLP_RemovesMultipleStopwords() {
        String descricao = "preciso de acesso ao sistema de vendas";
        String resultado = TitleGenerator.gerarTituloNLP(descricao);

        assertEquals("Preciso Acesso Sistema Vendas", resultado);
        assertFalse(resultado.contains("de"));
        assertFalse(resultado.contains("ao"));
    }

    @Test
    @DisplayName("Deve capitalizar primeira letra de cada palavra relevante")
    void testGerarTituloNLP_CapitalizesWords() {
        String descricao = "impressora travada na sala 101";
        String resultado = TitleGenerator.gerarTituloNLP(descricao);

        assertTrue(resultado.contains("Impressora"));
        assertTrue(resultado.contains("Travada"));
        assertTrue(resultado.contains("Sala"));
        assertTrue(resultado.contains("101"));
    }

    @Test
    @DisplayName("Deve manter números no título")
    void testGerarTituloNLP_KeepsNumbers() {
        String descricao = "erro 404 sistema versao 10";
        String resultado = TitleGenerator.gerarTituloNLP(descricao);

        assertTrue(resultado.contains("404"));
        assertTrue(resultado.contains("10")); // Números com 2+ caracteres são mantidos
        assertTrue(resultado.contains("Erro"));
    }

    @Test
    @DisplayName("Deve preservar acentos em português")
    void testGerarTituloNLP_PreservesAccents() {
        String descricao = "não consigo acessar área de configuração";
        String resultado = TitleGenerator.gerarTituloNLP(descricao);

        assertTrue(resultado.contains("Não"));
        assertTrue(resultado.contains("Área"));
        assertTrue(resultado.contains("Configuração"));
    }

    // =========================================================================
    // TESTES DE EDGE CASES - Null Safety e Validações
    // =========================================================================

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n", "   \t\n   "})
    @DisplayName("Deve retornar título padrão para entradas null, vazias ou apenas espaços")
    void testGerarTituloNLP_ReturnsDefaultForNullOrEmpty(String descricao) {
        String resultado = TitleGenerator.gerarTituloNLP(descricao);

        assertEquals("Título não informado", resultado);
    }

    @Test
    @DisplayName("Deve retornar título padrão quando descrição contém apenas stopwords")
    void testGerarTituloNLP_ReturnsDefaultForOnlyStopwords() {
        String descricao = "de do da para com no na";
        String resultado = TitleGenerator.gerarTituloNLP(descricao);

        assertEquals("Título não informado", resultado);
    }

    @Test
    @DisplayName("Deve retornar título padrão quando descrição contém apenas caracteres especiais")
    void testGerarTituloNLP_ReturnsDefaultForOnlySpecialChars() {
        String descricao = "!@#$%^&*()_+-={}[]|\\:;\"'<>,.?/";
        String resultado = TitleGenerator.gerarTituloNLP(descricao);

        assertEquals("Título não informado", resultado);
    }

    @Test
    @DisplayName("Deve lidar com palavras de uma letra após stopword removal")
    void testGerarTituloNLP_HandlesOneLetterWords() {
        String descricao = "a b c d e f";
        String resultado = TitleGenerator.gerarTituloNLP(descricao);

        // Todas são stopwords ou muito curtas, deve retornar padrão
        assertEquals("Título não informado", resultado);
    }

    // =========================================================================
    // TESTES DE NORMALIZAÇÃO - Limpeza de Caracteres
    // =========================================================================

    @Test
    @DisplayName("Deve remover caracteres especiais mantendo apenas alfanuméricos")
    void testGerarTituloNLP_RemovesSpecialCharacters() {
        String descricao = "computador!!! não@#$ liga%%%";
        String resultado = TitleGenerator.gerarTituloNLP(descricao);

        assertEquals("Computador Não Liga", resultado);
        assertFalse(resultado.contains("!"));
        assertFalse(resultado.contains("@"));
        assertFalse(resultado.contains("%"));
    }

    @Test
    @DisplayName("Deve normalizar múltiplos espaços em um único espaço")
    void testGerarTituloNLP_NormalizesMultipleSpaces() {
        String descricao = "computador    não     liga";
        String resultado = TitleGenerator.gerarTituloNLP(descricao);

        assertEquals("Computador Não Liga", resultado);
        assertFalse(resultado.contains("  ")); // Sem espaços duplos
    }

    @Test
    @DisplayName("Deve remover espaços no início e fim")
    void testGerarTituloNLP_TrimsSpaces() {
        String descricao = "   computador não liga   ";
        String resultado = TitleGenerator.gerarTituloNLP(descricao);

        assertEquals("Computador Não Liga", resultado);
        assertFalse(resultado.startsWith(" "));
        assertFalse(resultado.endsWith(" "));
    }

    // =========================================================================
    // TESTES DE LIMITAÇÃO - Tamanho Máximo de Título
    // =========================================================================

    @Test
    @DisplayName("Deve limitar título a no máximo 8 palavras")
    void testGerarTituloNLP_LimitsToMaxWords() {
        String descricao = "primeiro segundo terceiro quarto quinto sexto setimo oitavo nono decimo";
        String resultado = TitleGenerator.gerarTituloNLP(descricao);

        // Conta quantidade de palavras no resultado
        long wordCount = resultado.split("\\s+").length;
        assertTrue(wordCount <= 8, "Título deve ter no máximo 8 palavras, mas tem: " + wordCount);
    }

    @Test
    @DisplayName("Deve manter título curto quando descrição é pequena")
    void testGerarTituloNLP_KeepsShortTitles() {
        String descricao = "impressora travada";
        String resultado = TitleGenerator.gerarTituloNLP(descricao);

        assertEquals("Impressora Travada", resultado);
        assertEquals(2, resultado.split("\\s+").length);
    }

    // =========================================================================
    // TESTES PARAMETRIZADOS - Casos Reais de Uso
    // =========================================================================

    @ParameterizedTest
    @CsvSource({
            "'meu computador não liga', 'Meu Computador Não Liga'",
            "'preciso de acesso ao sistema', 'Preciso Acesso Sistema'",
            "'impressora com papel preso', 'Impressora Papel Preso'",
            "'erro ao acessar email', 'Erro Acessar Email'",
            "'senha bloqueada', 'Senha Bloqueada'",
            "'internet lenta', 'Internet Lenta'",
            "'tela azul da morte', 'Tela Azul Morte'",
            "'mouse sem fio não funciona', 'Mouse Fio Não Funciona'",
            "'teclado derramou agua', 'Teclado Derramou Agua'",
            "'monitor piscando', 'Monitor Piscando'"
    })
    @DisplayName("Deve gerar títulos corretos para casos de uso reais")
    void testGerarTituloNLP_RealWorldCases(String descricao, String tituloEsperado) {
        String resultado = TitleGenerator.gerarTituloNLP(descricao);

        assertEquals(tituloEsperado, resultado);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "COMPUTADOR NÃO LIGA",
            "computador não liga",
            "CoMpUtAdOr NãO lIgA",
            "COMPUTADOR não LIGA"
    })
    @DisplayName("Deve normalizar diferentes capitalizações para o mesmo resultado")
    void testGerarTituloNLP_NormalizesCasing(String descricao) {
        String resultado = TitleGenerator.gerarTituloNLP(descricao);

        assertEquals("Computador Não Liga", resultado);
    }

    // =========================================================================
    // TESTES DE SEGURANÇA - Prevenção de Injeção
    // =========================================================================

    @Test
    @DisplayName("Deve remover caracteres HTML mas manter palavras resultantes")
    void testGerarTituloNLP_RemovesHtmlInjection() {
        String descricao = "<script>alert('xss')</script> computador não liga";
        String resultado = TitleGenerator.gerarTituloNLP(descricao);

        // Remove os caracteres especiais < > ' ( )
        assertFalse(resultado.contains("<"));
        assertFalse(resultado.contains(">"));
        assertFalse(resultado.contains("'"));
        // Mas a palavra "script" se torna uma palavra válida após normalização
        // Nota: Sanitização de HTML deve ser feita ANTES com InputSanitizer
        assertTrue(resultado.contains("Computador"));
    }

    @Test
    @DisplayName("Deve remover tentativas de injeção SQL")
    void testGerarTituloNLP_RemovesSqlInjection() {
        String descricao = "'; DROP TABLE users; -- computador não liga";
        String resultado = TitleGenerator.gerarTituloNLP(descricao);

        assertFalse(resultado.contains(";"));
        assertFalse(resultado.contains("'"));
        assertFalse(resultado.contains("--"));
        assertTrue(resultado.contains("Computador"));
    }

    // =========================================================================
    // TESTES DE PALAVRAS CURTAS - Filtro de Tamanho Mínimo
    // =========================================================================

    @Test
    @DisplayName("Deve filtrar palavras muito curtas (menos de 2 caracteres)")
    void testGerarTituloNLP_FiltersShortWords() {
        String descricao = "pc ta travado";
        String resultado = TitleGenerator.gerarTituloNLP(descricao);

        // "ta" tem 2 caracteres, então é mantida
        // "pc" tem 2 caracteres, então é mantida
        assertTrue(resultado.contains("Pc"));
        assertTrue(resultado.contains("Ta"));
        assertTrue(resultado.contains("Travado"));
    }

    @Test
    @DisplayName("Deve manter palavras com exatamente 2 caracteres")
    void testGerarTituloNLP_KeepsTwoCharWords() {
        String descricao = "pc tv hd";
        String resultado = TitleGenerator.gerarTituloNLP(descricao);

        assertTrue(resultado.contains("Pc"));
        assertTrue(resultado.contains("Tv"));
        assertTrue(resultado.contains("Hd"));
    }

    // =========================================================================
    // TESTES DE UNICODE E CARACTERES ESPECIAIS PT-BR
    // =========================================================================

    @Test
    @DisplayName("Deve preservar caracteres especiais do português (ç, ã, õ, etc)")
    void testGerarTituloNLP_PreservesPortugueseChars() {
        String descricao = "configuração não funciona em são paulo";
        String resultado = TitleGenerator.gerarTituloNLP(descricao);

        assertTrue(resultado.contains("Configuração"));
        assertTrue(resultado.contains("São"));
        assertTrue(resultado.contains("Paulo"));
    }

    @Test
    @DisplayName("Deve lidar com todos os tipos de acentos")
    void testGerarTituloNLP_HandlesAllAccents() {
        String descricao = "área específica com códigos únicos ótimos";
        String resultado = TitleGenerator.gerarTituloNLP(descricao);

        assertTrue(resultado.contains("Área"));
        assertTrue(resultado.contains("Específica"));
        assertTrue(resultado.contains("Códigos"));
        assertTrue(resultado.contains("Únicos"));
        assertTrue(resultado.contains("Ótimos"));
    }

    // =========================================================================
    // TESTES DE QUALIDADE - Verificação de Formato
    // =========================================================================

    @Test
    @DisplayName("Título gerado não deve ter espaços no início ou fim")
    void testGerarTituloNLP_NoLeadingTrailingSpaces() {
        String descricao = "   computador não liga   ";
        String resultado = TitleGenerator.gerarTituloNLP(descricao);

        assertEquals(resultado, resultado.trim());
    }

    @Test
    @DisplayName("Título gerado não deve ter espaços duplos")
    void testGerarTituloNLP_NoDoubleSpaces() {
        String descricao = "computador    não    liga";
        String resultado = TitleGenerator.gerarTituloNLP(descricao);

        assertFalse(resultado.contains("  "));
    }

    @Test
    @DisplayName("Título gerado deve começar com letra maiúscula")
    void testGerarTituloNLP_StartsWithCapitalLetter() {
        String descricao = "computador não liga";
        String resultado = TitleGenerator.gerarTituloNLP(descricao);

        assertTrue(Character.isUpperCase(resultado.charAt(0)));
    }

    @Test
    @DisplayName("Cada palavra do título deve começar com maiúscula")
    void testGerarTituloNLP_EachWordCapitalized() {
        String descricao = "computador não liga impressora travada";
        String resultado = TitleGenerator.gerarTituloNLP(descricao);

        String[] palavras = resultado.split("\\s+");
        for (String palavra : palavras) {
            assertTrue(Character.isUpperCase(palavra.charAt(0)),
                    "Palavra '" + palavra + "' não começa com maiúscula");
        }
    }

    // =========================================================================
    // TESTES DE MÉTODO DEPRECATED - adicionarStopword()
    // =========================================================================

    @Test
    @DisplayName("Deve lançar UnsupportedOperationException ao tentar adicionar stopword")
    void testAdicionarStopword_ThrowsException() {
        assertThrows(UnsupportedOperationException.class, () -> {
            TitleGenerator.adicionarStopword("teste");
        });
    }
}
