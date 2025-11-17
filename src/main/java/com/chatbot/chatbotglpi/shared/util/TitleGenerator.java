package com.chatbot.chatbotglpi.shared.util;

import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Classe utilitária para geração automática de títulos a partir de descrições.
 *
 * Esta classe implementa um algoritmo simples de NLP (Natural Language Processing)
 * que remove stopwords e capitaliza palavras relevantes para criar títulos
 * concisos e informativos.
 *
 * <p>A anotação @UtilityClass do Lombok torna esta classe final, com construtor
 * privado e todos os métodos estáticos automaticamente.</p>
 *
 * <p><strong>Princípio SOLID aplicado:</strong></p>
 * <ul>
 *   <li>Single Responsibility: Responsável APENAS por geração de títulos</li>
 *   <li>Separada de InputSanitizer que tem responsabilidade de sanitização</li>
 * </ul>
 *
 * <p><strong>Exemplos de uso:</strong></p>
 * <pre>
 * String titulo = TitleGenerator.gerarTituloNLP("Meu computador não liga");
 * // Retorna: "Computador Não Liga"
 *
 * String titulo = TitleGenerator.gerarTituloNLP("Preciso de acesso ao sistema de vendas");
 * // Retorna: "Preciso Acesso Sistema Vendas"
 * </pre>
 *
 * @see InputSanitizer
 * @since 1.0
 */
@UtilityClass
public class TitleGenerator {

    /**
     * Lista de stopwords em português que devem ser removidas do título.
     *
     * <p>Stopwords são palavras muito comuns que não agregam significado
     * semântico relevante ao título (artigos, preposições, conjunções).</p>
     *
     * <p>Esta lista pode ser expandida conforme necessário para melhorar
     * a qualidade dos títulos gerados.</p>
     */
    private static final Set<String> STOP_WORDS = Set.of(
            "a", "o", "de", "da", "do", "dos", "das",
            "para", "com", "sem", "por", "em", "no", "na", "nos", "nas",
            "um", "uma", "uns", "umas",
            "ao", "aos", "à", "às",
            "e", "ou", "mas", "que", "se"
    );

    /**
     * Comprimento mínimo de palavra para ser considerada no título.
     * Palavras muito curtas geralmente não agregam valor semântico.
     */
    private static final int MIN_WORD_LENGTH = 2;

    /**
     * Número máximo de palavras no título gerado.
     * Evita títulos muito longos que perdem concisão.
     */
    private static final int MAX_TITLE_WORDS = 8;

    /**
     * Título padrão retornado quando a descrição é nula, vazia ou inválida.
     */
    private static final String DEFAULT_TITLE = "Título não informado";

    /**
     * Gera um título automaticamente a partir de uma descrição usando técnicas de NLP.
     *
     * <p><strong>Algoritmo:</strong></p>
     * <ol>
     *   <li>Valida se a descrição não é nula ou vazia</li>
     *   <li>Normaliza o texto (lowercase, remove caracteres especiais)</li>
     *   <li>Divide em palavras (tokenização)</li>
     *   <li>Remove stopwords (palavras irrelevantes)</li>
     *   <li>Filtra palavras muito curtas</li>
     *   <li>Limita número de palavras para manter concisão</li>
     *   <li>Capitaliza primeira letra de cada palavra relevante</li>
     *   <li>Retorna título formatado</li>
     * </ol>
     *
     * <p><strong>Validações implementadas:</strong></p>
     * <ul>
     *   <li>Null safety: retorna título padrão se descrição for null</li>
     *   <li>Empty check: retorna título padrão se descrição estiver vazia/em branco</li>
     *   <li>Word validation: ignora palavras vazias após split</li>
     *   <li>Length check: verifica comprimento de cada palavra antes de capitalizar</li>
     *   <li>Limit words: limita quantidade de palavras para evitar títulos longos</li>
     * </ul>
     *
     * @param descricao Descrição do problema ou solicitação do usuário
     * @return Título gerado automaticamente, capitalizado e formatado.
     *         Retorna "Título não informado" se a descrição for nula, vazia ou inválida.
     *
     * @throws NullPointerException Nunca é lançada - método é null-safe
     *
     * @see #STOP_WORDS
     * @see #DEFAULT_TITLE
     */
    public static String gerarTituloNLP(String descricao) {
        // ========================================
        // 1. VALIDAÇÃO DE ENTRADA
        // ========================================
        if (descricao == null || descricao.isBlank()) {
            return DEFAULT_TITLE;
        }

        // ========================================
        // 2. NORMALIZAÇÃO E TOKENIZAÇÃO
        // ========================================
        // Remove caracteres especiais mantendo apenas letras, números e espaços
        // Aceita caracteres acentuados (á-ú, à-ù, etc.)
        String normalized = descricao
                .toLowerCase()
                .replaceAll("[^a-záàâãéêíóôõúüç0-9 ]", "")
                .trim();

        // Se após normalização ficar vazio, retorna título padrão
        if (normalized.isEmpty()) {
            return DEFAULT_TITLE;
        }

        // Divide em palavras (split por espaços múltiplos)
        String[] palavras = normalized.split("\\s+");

        // ========================================
        // 3. FILTRAGEM E PROCESSAMENTO
        // ========================================
        String tituloGerado = Arrays.stream(palavras)
                // Remove palavras vazias (edge case de splits mal formados)
                .filter(palavra -> !palavra.isEmpty())
                // Remove stopwords (artigos, preposições, etc.)
                .filter(palavra -> !STOP_WORDS.contains(palavra))
                // Remove palavras muito curtas (geralmente sem significado)
                .filter(palavra -> palavra.length() >= MIN_WORD_LENGTH)
                // Limita número de palavras para manter título conciso
                .limit(MAX_TITLE_WORDS)
                // Capitaliza primeira letra de cada palavra
                .map(TitleGenerator::capitalizarPalavra)
                // Junta palavras com espaço
                .collect(Collectors.joining(" "))
                .trim();

        // ========================================
        // 4. VALIDAÇÃO FINAL
        // ========================================
        // Se após todo processamento não sobrou nenhuma palavra relevante,
        // retorna título padrão
        if (tituloGerado.isEmpty()) {
            return DEFAULT_TITLE;
        }

        return tituloGerado;
    }

    /**
     * Capitaliza a primeira letra de uma palavra.
     *
     * <p>Este método é null-safe e protegido contra strings vazias.</p>
     *
     * <p><strong>Exemplos:</strong></p>
     * <pre>
     * capitalizarPalavra("computador") → "Computador"
     * capitalizarPalavra("não") → "Não"
     * capitalizarPalavra("") → ""
     * capitalizarPalavra(null) → ""
     * </pre>
     *
     * @param palavra Palavra a ser capitalizada
     * @return Palavra com primeira letra maiúscula, ou string vazia se entrada inválida
     */
    private static String capitalizarPalavra(String palavra) {
        // Null safety e empty check
        if (palavra == null || palavra.isEmpty()) {
            return "";
        }

        // Se palavra tem apenas 1 caractere, converte para maiúsculo
        if (palavra.length() == 1) {
            return palavra.toUpperCase();
        }

        // Capitaliza primeira letra e mantém resto da palavra em lowercase
        return Character.toUpperCase(palavra.charAt(0)) + palavra.substring(1);
    }

    /**
     * Adiciona uma stopword customizada à lista de stopwords.
     *
     * <p><strong>Nota:</strong> Este método não está implementado pois {@link #STOP_WORDS}
     * é um Set imutável. Para adicionar stopwords customizadas, edite
     * diretamente a constante {@link #STOP_WORDS}.</p>
     *
     * <p>Em versões futuras, pode-se considerar tornar as stopwords configuráveis
     * via properties ou banco de dados.</p>
     *
     * @param stopword Stopword a ser adicionada
     * @deprecated Método não implementado - edite {@link #STOP_WORDS} diretamente
     */
    @Deprecated
    public static void adicionarStopword(String stopword) {
        throw new UnsupportedOperationException(
                "STOP_WORDS é imutável. Edite a constante diretamente no código."
        );
    }
}
