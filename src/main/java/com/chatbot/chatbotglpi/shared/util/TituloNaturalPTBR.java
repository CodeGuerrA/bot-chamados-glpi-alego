package com.chatbot.chatbotglpi.shared.util;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.text.Normalizer;
import java.util.*;

/**
 * Gera títulos humanizados a partir de descrições em português (pt-BR).
 * Regras principais:
 * - Prioriza pequenas frases substantivo + adjetivo, ex: "computador com tela preta".
 * - Captura padrões "não + verbo", ex: "não imprime".
 * - Fallback: pega primeiros tokens relevantes caso o POS-tagger falhe.
 * - Limita título a MAX_TITLE_WORDS palavras.
 */
@Component
public class TituloNaturalPTBR {

    private final TokenizerME tokenizer;
    private final POSTaggerME posTagger;

    private static final int MAX_TITLE_WORDS = 4;

    // Stopwords que NÃO devem aparecer no título
    private final Set<String> stopWords = new HashSet<>(Arrays.asList(
            "a", "o", "as", "os", "de", "do", "da", "dos", "das",
            "em", "no", "na", "nos", "nas", "para", "por",
            "e", "ou", "mas", "que", "se", "meu", "minha", "meus", "minhas",
            "seu", "sua", "seus", "suas", "este", "esta", "estes", "estas",
            "foi", "ser", "tem", "tá", "está", "vai", "vou",
            "um", "uma", "uns", "umas"
    ));

    // Conectores permitidos dentro de frases (ex: "computador com tela preta")
    private final Set<String> allowedInnerConnectors = new HashSet<>(Arrays.asList(
            "com", "sem", "ao", "no", "na", "em"
    ));

    // Verbos irrelevantes para títulos
    private final Set<String> ignoredVerbs = new HashSet<>(Arrays.asList(
            "ser", "estar", "ter", "ficar", "conseguir", "poder", "ir", "haver"
    ));

    public TituloNaturalPTBR() throws Exception {
        try (InputStream tokenModelIn = getClass().getResourceAsStream("/models/pt-token.bin");
             InputStream posModelIn = getClass().getResourceAsStream("/models/pt-pos-maxent.bin")) {

            tokenizer = new TokenizerME(new TokenizerModel(tokenModelIn));
            posTagger = new POSTaggerME(new POSModel(posModelIn));
        }
    }

    public String gerarTitulo(String descricao) {
        if (descricao == null || descricao.trim().isEmpty()) {
            return "Chamado";
        }

        String normalized = normalize(descricao);
        String[] tokens = tokenizer.tokenize(normalized.toLowerCase());
        String[] tags = posTagger.tag(tokens);

        List<String> candidates = new ArrayList<>();

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i].trim();
            if (token.isEmpty() || stopWords.contains(token)) continue;

            String tag = tags[i] == null ? "" : tags[i];

            // 1) Construção de pequenas frases substantivo + adjetivo
            if (tag.startsWith("N")) {
                StringBuilder phrase = new StringBuilder(token);
                int j = i + 1;

                // Adjetivos subsequentes
                while (j < tokens.length) {
                    String next = tokens[j].trim();
                    String nextTag = tags[j] == null ? "" : tags[j];
                    if (nextTag.startsWith("ADJ") || nextTag.startsWith("A")) {
                        phrase.append(" ").append(next);
                        j++;
                    } else break;
                }

                // Permitindo "com" ou "sem" + substantivo/adjetivo
                if (j + 1 < tokens.length) {
                    String mid = tokens[j].trim();
                    String after = tokens[j + 1].trim();
                    String afterTag = tags[j + 1] == null ? "" : tags[j + 1];

                    if (allowedInnerConnectors.contains(mid) &&
                            (afterTag.startsWith("N") || afterTag.startsWith("ADJ") || afterTag.startsWith("A"))) {
                        phrase.append(" ").append(mid).append(" ").append(after);
                        int k = j + 2;
                        while (k < tokens.length) {
                            String t = tokens[k].trim();
                            String tTag = tags[k] == null ? "" : tags[k];
                            if (tTag.startsWith("ADJ") || tTag.startsWith("A") || tTag.startsWith("N")) {
                                phrase.append(" ").append(t);
                                k++;
                            } else break;
                        }
                    }
                }

                candidates.add(phrase.toString());
            }

            // 2) Padrão "não + verbo"
            if ((token.equals("nao") || token.equals("não")) && i + 1 < tokens.length) {
                String nextTag = tags[i + 1] == null ? "" : tags[i + 1];
                String nextToken = tokens[i + 1].trim();
                if (nextTag.startsWith("V") && !ignoredVerbs.contains(nextToken)) {
                    candidates.add("não " + nextToken);
                }
            }
        }

        // 3) Seleciona frase mais natural com pelo menos 2 palavras
        String chosen = null;
        for (String c : candidates) {
            if (c == null || c.trim().isEmpty()) continue;
            int words = c.trim().split("\\s+").length;
            if (words >= 2) {
                chosen = c;
                break;
            }
            if (chosen == null) chosen = c;
        }

        // 4) Fallback: primeiros tokens significativos
        if (chosen == null || chosen.trim().isEmpty()) {
            LinkedHashSet<String> picked = new LinkedHashSet<>();
            for (String tk : tokens) {
                String t = tk.trim();
                if (t.isEmpty() || stopWords.contains(t)) continue;
                if (isMeaningful(t)) {
                    picked.add(t);
                }
                if (picked.size() >= MAX_TITLE_WORDS) break;
            }
            if (!picked.isEmpty()) {
                chosen = String.join(" ", picked);
            }
        }

        if (chosen == null || chosen.trim().isEmpty()) {
            return "Chamado";
        }

        // 5) Limitar a MAX_TITLE_WORDS
        String[] chosenWords = chosen.trim().split("\\s+");
        StringBuilder finalTitle = new StringBuilder();
        int limit = Math.min(chosenWords.length, MAX_TITLE_WORDS);
        for (int i = 0; i < limit; i++) {
            if (i > 0) finalTitle.append(" ");
            finalTitle.append(chosenWords[i]);
        }

        String resultado = finalTitle.toString().replaceAll("\\s+", " ").trim();
        return resultado.substring(0, 1).toUpperCase() + resultado.substring(1);
    }

    private boolean isMeaningful(String token) {
        if (token.length() <= 2) return false;
        return token.matches(".*[a-zA-Z0-9].*") && !token.matches("\\d+");
    }

    private String normalize(String input) {
        String n = Normalizer.normalize(input, Normalizer.Form.NFD);
        return n.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}