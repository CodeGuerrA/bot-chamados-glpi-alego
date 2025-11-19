package com.chatbot.chatbotglpi.shared.util;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Gerador avançado de títulos humanizados usando NLP para português (pt-BR).
 *
 * Técnicas aplicadas:
 * - POS Tagging (Apache OpenNLP)
 * - Extração de entidades nomeadas (equipamentos, sistemas)
 * - Análise de padrões sintáticos complexos
 * - Ranking de candidatos por relevância
 * - Templates contextuais
 * - Similaridade de Levenshtein para deduplicação
 *
 * Exemplos de saída:
 * - "impressora não imprime" → "Impressora não imprimindo"
 * - "computador está com tela preta" → "Computador com tela preta"
 * - "meu computador esta com a tela preta e parou de funcionar" → "Computador com tela preta"
 * - "não consigo acessar o sistema de vendas" → "Sistema vendas inacessível"
 * - "internet lenta sala 305" → "Internet lenta sala 305"
 * - "notebook parou de funcionar" → "Notebook parou de funcionar"
 */
@Component
public class TituloNaturalPTBR {

    private final TokenizerME tokenizer;
    private final POSTaggerME posTagger;
    private final LevenshteinDistance levenshtein;

    private static final int MAX_TITLE_WORDS = 5;
    private static final int MIN_CANDIDATE_SCORE = 3;

    // === DICIONÁRIOS ESPECIALIZADOS ===

    /**
     * Equipamentos e sistemas de TI (alto valor semântico)
     */
    private final Set<String> techEquipment = new HashSet<>(Arrays.asList(
            "impressora", "computador", "notebook", "monitor", "teclado", "mouse",
            "servidor", "wifi", "internet", "rede", "email", "sistema", "software",
            "aplicativo", "programa", "scanner", "telefone", "ramal", "headset",
            "webcam", "microfone", "projetor", "nobreak", "estabilizador",
            "hd", "ssd", "memoria", "processador", "placa", "cabo", "switch",
            "roteador", "modem", "access point", "firewall", "backup"
    ));

    /**
     * Verbos de problema (indicam falha/defeito)
     */
    private final Map<String, String> problemVerbs = new HashMap<>() {{
        put("imprime", "imprimindo");
        put("imprimir", "imprimindo");
        put("liga", "ligando");
        put("ligar", "ligando");
        put("funciona", "funcionando");
        put("funcionar", "funcionando");
        put("parou", "parado");
        put("parar", "parado");
        put("para", "parando");
        put("travou", "travado");
        put("travar", "travado");
        put("trava", "travando");
        put("conecta", "conectando");
        put("conectar", "conectando");
        put("abre", "abrindo");
        put("abrir", "abrindo");
        put("carrega", "carregando");
        put("carregar", "carregando");
        put("acessa", "acessando");
        put("acessar", "acessando");
        put("envia", "enviando");
        put("enviar", "enviando");
        put("salva", "salvando");
        put("salvar", "salvando");
        put("instala", "instalando");
        put("instalar", "instalando");
        put("atualiza", "atualizando");
        put("atualizar", "atualizando");
        put("sincroniza", "sincronizando");
        put("sincronizar", "sincronizando");
        put("responde", "respondendo");
        put("responder", "respondendo");
    }};

    /**
     * Adjetivos de problema
     */
    private final Set<String> problemAdjectives = new HashSet<>(Arrays.asList(
            "lento", "lenta", "travado", "travada", "quebrado", "quebrada",
            "parado", "parada", "offline", "desligado", "desligada",
            "inacessível", "indisponível", "defeituoso", "defeituosa",
            "corrompido", "corrompida", "danificado", "danificada",
            "bloqueado", "bloqueada", "suspenso", "suspensa",
            "preta", "preto", "azul", "branca", "branco", "congelado", "congelada"
    ));

    /**
     * Substantivos de problema (tokens únicos)
     */
    private final Set<String> problemNouns = new HashSet<>(Arrays.asList(
            "erro", "falha", "problema", "defeito", "bug", "crash",
            "travamento", "lentidão", "delay", "lag", "timeout",
            "mensagem", "alerta", "aviso", "tela"
    ));

    /**
     * Expressões compostas de problema (multi-token)
     */
    private final Map<String, String> compoundProblems = new HashMap<>() {{
        put("tela preta", "tela preta");
        put("tela azul", "tela azul");
        put("tela branca", "tela branca");
        put("tela congelada", "tela congelada");
        put("sem internet", "sem internet");
        put("sem rede", "sem rede");
        put("sem acesso", "sem acesso");
        put("sem conexao", "sem conexão");
        put("sem som", "sem som");
        put("sem audio", "sem áudio");
        put("sem imagem", "sem imagem");
        put("parou de funcionar", "parou de funcionar");
        put("parou de", "parou");
    }};

    /**
     * Stopwords (não devem iniciar ou dominar o título)
     */
    private final Set<String> stopWords = new HashSet<>(Arrays.asList(
            "a", "o", "as", "os", "de", "do", "da", "dos", "das",
            "em", "no", "na", "nos", "nas", "para", "por", "pelo", "pela",
            "e", "ou", "mas", "que", "se", "me", "te", "lhe",
            "meu", "minha", "meus", "minhas", "seu", "sua", "seus", "suas",
            "este", "esta", "estes", "estas", "esse", "essa", "esses", "essas",
            "aquele", "aquela", "aqueles", "aquelas",
            "foi", "ser", "estar", "ter", "haver", "tem", "tá", "está",
            "vai", "vou", "são", "é", "era", "estava", "tenho",
            "um", "uma", "uns", "umas", "muito", "muita", "pouco", "pouca",
            "pode", "posso", "consegue", "consigo", "quer", "quero",
            "precisa", "preciso", "gostaria", "favor", "ajuda", "ajudar"
    ));

    /**
     * Conectores permitidos dentro de frases
     */
    private final Set<String> allowedConnectors = new HashSet<>(Arrays.asList(
            "com", "sem", "ao", "no", "na", "em", "do", "da", "de"
    ));

    public TituloNaturalPTBR() throws Exception {
        try (InputStream tokenModelIn = getClass().getResourceAsStream("/models/pt-token.bin");
             InputStream posModelIn = getClass().getResourceAsStream("/models/pt-pos-maxent.bin")) {

            tokenizer = new TokenizerME(new TokenizerModel(tokenModelIn));
            posTagger = new POSTaggerME(new POSModel(posModelIn));
            levenshtein = new LevenshteinDistance();
        }
    }

    /**
     * Gera título humanizado e contextualizado a partir da descrição
     */
    public String gerarTitulo(String descricao) {
        if (descricao == null || descricao.trim().isEmpty()) {
            return "Chamado de suporte";
        }

        // Normaliza e tokeniza
        String normalized = normalize(descricao);
        String[] tokens = tokenizer.tokenize(normalized.toLowerCase());
        String[] tags = posTagger.tag(tokens);

        // Coleta todos os candidatos possíveis
        List<TitleCandidate> candidates = new ArrayList<>();

        // Estratégia 0: Detecta expressões compostas de problema (PRIORIDADE MÁXIMA)
        candidates.addAll(extractCompoundProblems(tokens, tags));

        // Estratégia 1: Detecta padrões "não + verbo"
        candidates.addAll(extractNegationPatterns(tokens, tags));

        // Estratégia 2: Equipamento + problema
        candidates.addAll(extractEquipmentProblems(tokens, tags));

        // Estratégia 3: Substantivo + adjetivo de problema
        candidates.addAll(extractNounAdjectivePatterns(tokens, tags));

        // Estratégia 4: Verbo + objeto direto
        candidates.addAll(extractVerbObjectPatterns(tokens, tags));

        // Estratégia 5: Localização + problema (ex: "internet lenta sala 305")
        candidates.addAll(extractLocationPatterns(tokens, tags, descricao));

        // Estratégia 6: Extração de entidades técnicas
        candidates.addAll(extractTechnicalEntities(tokens, tags));

        // Rank candidatos por score
        candidates.sort(Comparator.comparingInt(TitleCandidate::getScore).reversed());

        // Remove duplicatas similares (Levenshtein)
        List<TitleCandidate> deduped = deduplicateCandidates(candidates);

        // Seleciona o melhor candidato
        if (!deduped.isEmpty() && deduped.get(0).getScore() >= MIN_CANDIDATE_SCORE) {
            return formatTitle(deduped.get(0).getText());
        }

        // Fallback: extrai tokens mais relevantes
        return fallbackTitle(tokens, tags);
    }

    /**
     * Estratégia 0: Detecta expressões compostas de problema
     * Ex: "computador com tela preta" → "Computador com tela preta"
     * Ex: "internet parou de funcionar" → "Internet parou de funcionar"
     */
    private List<TitleCandidate> extractCompoundProblems(String[] tokens, String[] tags) {
        List<TitleCandidate> results = new ArrayList<>();

        // Procura equipamento seguido de expressões compostas
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i].trim();

            // Se encontrar um equipamento
            if (techEquipment.contains(token)) {
                // Procura por expressões compostas próximas
                for (int j = i + 1; j < Math.min(i + 7, tokens.length); j++) {
                    // Tenta formar expressões de 2, 3 e 4 palavras
                    for (int wordCount = 4; wordCount >= 2; wordCount--) {
                        if (j + wordCount <= tokens.length) {
                            StringBuilder compound = new StringBuilder();
                            for (int k = j; k < j + wordCount; k++) {
                                if (k > j) compound.append(" ");
                                compound.append(tokens[k].trim());
                            }

                            String compoundStr = compound.toString();
                            if (compoundProblems.containsKey(compoundStr)) {
                                // Monta o título: equipamento + conector (se houver) + problema
                                StringBuilder phrase = new StringBuilder(token);

                                // Adiciona tokens intermediários (conectores)
                                for (int k = i + 1; k < j; k++) {
                                    String intermediate = tokens[k].trim();
                                    if (allowedConnectors.contains(intermediate) || intermediate.equals("a")) {
                                        phrase.append(" ").append(intermediate);
                                    }
                                }

                                phrase.append(" ").append(compoundProblems.get(compoundStr));
                                results.add(new TitleCandidate(phrase.toString(), 15)); // Score máximo
                                break;
                            }
                        }
                    }
                }
            }

            // Procura expressões compostas sem equipamento no início
            for (int wordCount = 4; wordCount >= 2; wordCount--) {
                if (i + wordCount <= tokens.length) {
                    StringBuilder compound = new StringBuilder();
                    for (int k = i; k < i + wordCount; k++) {
                        if (k > i) compound.append(" ");
                        compound.append(tokens[k].trim());
                    }

                    String compoundStr = compound.toString();
                    if (compoundProblems.containsKey(compoundStr)) {
                        results.add(new TitleCandidate(compoundProblems.get(compoundStr), 12));
                    }
                }
            }
        }

        return results;
    }

    /**
     * Estratégia 1: Padrões de negação (não + verbo)
     * Ex: "não imprime" → "Não imprimindo"
     */
    private List<TitleCandidate> extractNegationPatterns(String[] tokens, String[] tags) {
        List<TitleCandidate> results = new ArrayList<>();

        for (int i = 0; i < tokens.length - 1; i++) {
            String token = tokens[i].trim();

            if (token.equals("não") || token.equals("nao")) {
                String nextToken = tokens[i + 1].trim();
                String nextTag = tags[i + 1];

                // Verbo após "não"
                if (nextTag != null && nextTag.startsWith("V")) {
                    String gerund = problemVerbs.getOrDefault(nextToken, nextToken);

                    // Verifica se há objeto direto após o verbo
                    StringBuilder phrase = new StringBuilder("não " + gerund);
                    int score = 8; // Alta prioridade para negações

                    // Adiciona objeto se existir
                    if (i + 2 < tokens.length) {
                        String objToken = tokens[i + 2].trim();
                        String objTag = tags[i + 2];

                        if (objTag != null && objTag.startsWith("N") && !stopWords.contains(objToken)) {
                            phrase.append(" ").append(objToken);
                            score += 2;
                        }
                    }

                    results.add(new TitleCandidate(phrase.toString(), score));
                }
            }
        }

        return results;
    }

    /**
     * Estratégia 2: Equipamento + problema
     * Ex: "impressora não imprime", "internet lenta"
     */
    private List<TitleCandidate> extractEquipmentProblems(String[] tokens, String[] tags) {
        List<TitleCandidate> results = new ArrayList<>();

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i].trim();

            if (techEquipment.contains(token)) {
                StringBuilder phrase = new StringBuilder(token);
                int score = 10; // Equipamentos têm alta prioridade

                // Procura adjetivos de problema próximos
                for (int j = i + 1; j < Math.min(i + 4, tokens.length); j++) {
                    String nextToken = tokens[j].trim();

                    if (problemAdjectives.contains(nextToken)) {
                        phrase.append(" ").append(nextToken);
                        score += 3;
                        break;
                    }

                    // Conectores permitidos
                    if (allowedConnectors.contains(nextToken) && j + 1 < tokens.length) {
                        String afterConn = tokens[j + 1].trim();
                        if (problemNouns.contains(afterConn) || problemAdjectives.contains(afterConn)) {
                            phrase.append(" ").append(nextToken).append(" ").append(afterConn);
                            score += 2;
                            break;
                        }
                    }

                    // "não + verbo" após equipamento
                    if ((nextToken.equals("não") || nextToken.equals("nao")) && j + 1 < tokens.length) {
                        String verb = tokens[j + 1].trim();
                        String gerund = problemVerbs.getOrDefault(verb, verb);
                        phrase.append(" ").append(nextToken).append(" ").append(gerund);
                        score += 4;
                        break;
                    }
                }

                if (phrase.toString().split("\\s+").length >= 2) {
                    results.add(new TitleCandidate(phrase.toString(), score));
                }
            }
        }

        return results;
    }

    /**
     * Estratégia 3: Substantivo + adjetivo
     * Ex: "sistema bloqueado", "rede indisponível"
     */
    private List<TitleCandidate> extractNounAdjectivePatterns(String[] tokens, String[] tags) {
        List<TitleCandidate> results = new ArrayList<>();

        for (int i = 0; i < tokens.length - 1; i++) {
            String token = tokens[i].trim();
            String tag = tags[i];

            if (tag != null && tag.startsWith("N") && !stopWords.contains(token)) {
                StringBuilder phrase = new StringBuilder(token);
                int score = 5;

                // Adjetivos subsequentes
                for (int j = i + 1; j < Math.min(i + 3, tokens.length); j++) {
                    String nextToken = tokens[j].trim();
                    String nextTag = tags[j];

                    if (nextTag != null && (nextTag.startsWith("ADJ") || nextTag.startsWith("A"))) {
                        phrase.append(" ").append(nextToken);
                        score += 2;

                        if (problemAdjectives.contains(nextToken)) {
                            score += 2;
                        }
                    } else if (!allowedConnectors.contains(nextToken)) {
                        break;
                    }
                }

                if (phrase.toString().split("\\s+").length >= 2) {
                    results.add(new TitleCandidate(phrase.toString(), score));
                }
            }
        }

        return results;
    }

    /**
     * Estratégia 4: Verbo + objeto direto
     * Ex: "acessar sistema", "enviar email"
     */
    private List<TitleCandidate> extractVerbObjectPatterns(String[] tokens, String[] tags) {
        List<TitleCandidate> results = new ArrayList<>();

        for (int i = 0; i < tokens.length - 1; i++) {
            String token = tokens[i].trim();
            String tag = tags[i];

            if (tag != null && tag.startsWith("V") && problemVerbs.containsKey(token)) {
                String gerund = problemVerbs.get(token);

                // Procura objeto direto
                for (int j = i + 1; j < Math.min(i + 3, tokens.length); j++) {
                    String objToken = tokens[j].trim();
                    String objTag = tags[j];

                    if (stopWords.contains(objToken)) continue;

                    if (objTag != null && objTag.startsWith("N")) {
                        String phrase = gerund + " " + objToken;
                        int score = 6;

                        if (techEquipment.contains(objToken)) {
                            score += 3;
                        }

                        results.add(new TitleCandidate(phrase, score));
                        break;
                    }
                }
            }
        }

        return results;
    }

    /**
     * Estratégia 5: Padrões com localização
     * Ex: "internet lenta sala 305"
     */
    private List<TitleCandidate> extractLocationPatterns(String[] tokens, String[] tags, String original) {
        List<TitleCandidate> results = new ArrayList<>();

        // Regex para detectar localizações (sala/andar + número)
        Pattern locationPattern = Pattern.compile("(?i)(sala|andar|predio|bloco)\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = locationPattern.matcher(original);

        if (matcher.find()) {
            String location = matcher.group(0);

            // Procura problema antes da localização
            for (int i = 0; i < tokens.length - 1; i++) {
                String token = tokens[i].trim();

                if (techEquipment.contains(token)) {
                    // Procura adjetivo de problema
                    for (int j = i + 1; j < Math.min(i + 3, tokens.length); j++) {
                        String adj = tokens[j].trim();
                        if (problemAdjectives.contains(adj)) {
                            String phrase = token + " " + adj + " " + location;
                            results.add(new TitleCandidate(phrase, 9));
                            break;
                        }
                    }
                }
            }
        }

        return results;
    }

    /**
     * Estratégia 6: Extrai entidades técnicas puras
     */
    private List<TitleCandidate> extractTechnicalEntities(String[] tokens, String[] tags) {
        List<TitleCandidate> results = new ArrayList<>();

        StringBuilder currentEntity = new StringBuilder();
        int score = 4;

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i].trim();

            if (techEquipment.contains(token) || problemNouns.contains(token)) {
                if (currentEntity.length() > 0) {
                    currentEntity.append(" ");
                }
                currentEntity.append(token);
                score++;
            } else if (currentEntity.length() > 0) {
                // Finaliza entidade
                results.add(new TitleCandidate(currentEntity.toString(), score));
                currentEntity = new StringBuilder();
                score = 4;
            }
        }

        if (currentEntity.length() > 0) {
            results.add(new TitleCandidate(currentEntity.toString(), score));
        }

        return results;
    }

    /**
     * Remove candidatos muito similares (Levenshtein Distance)
     */
    private List<TitleCandidate> deduplicateCandidates(List<TitleCandidate> candidates) {
        List<TitleCandidate> result = new ArrayList<>();

        for (TitleCandidate candidate : candidates) {
            boolean isDuplicate = false;

            for (TitleCandidate existing : result) {
                int distance = levenshtein.apply(candidate.getText(), existing.getText());
                int maxLen = Math.max(candidate.getText().length(), existing.getText().length());

                // Se similaridade > 70%, considera duplicata
                if (distance * 100.0 / maxLen < 30) {
                    isDuplicate = true;
                    // Mantém o de maior score
                    if (candidate.getScore() > existing.getScore()) {
                        result.remove(existing);
                        result.add(candidate);
                    }
                    break;
                }
            }

            if (!isDuplicate) {
                result.add(candidate);
            }
        }

        return result;
    }

    /**
     * Fallback: extrai tokens mais relevantes
     */
    private String fallbackTitle(String[] tokens, String[] tags) {
        List<String> relevant = new ArrayList<>();

        for (int i = 0; i < tokens.length && relevant.size() < MAX_TITLE_WORDS; i++) {
            String token = tokens[i].trim();

            if (stopWords.contains(token) || token.length() <= 2) continue;

            if (techEquipment.contains(token) ||
                problemNouns.contains(token) ||
                problemAdjectives.contains(token)) {
                relevant.add(token);
            } else if (tags[i] != null && (tags[i].startsWith("N") || tags[i].startsWith("V"))) {
                relevant.add(token);
            }
        }

        if (relevant.isEmpty()) {
            return "Chamado de suporte";
        }

        return formatTitle(String.join(" ", relevant));
    }

    /**
     * Formata título final (primeira letra maiúscula, limita palavras)
     */
    private String formatTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "Chamado de suporte";
        }

        String cleaned = title.trim().replaceAll("\\s+", " ");
        String[] words = cleaned.split("\\s+");

        // Limita a MAX_TITLE_WORDS
        int limit = Math.min(words.length, MAX_TITLE_WORDS);
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < limit; i++) {
            if (i > 0) result.append(" ");
            result.append(words[i]);
        }

        String finalTitle = result.toString();

        // Primeira letra maiúscula
        if (finalTitle.length() > 0) {
            return finalTitle.substring(0, 1).toUpperCase() + finalTitle.substring(1);
        }

        return finalTitle;
    }

    /**
     * Normaliza texto (remove acentos)
     */
    private String normalize(String input) {
        String n = Normalizer.normalize(input, Normalizer.Form.NFD);
        return n.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    /**
     * Classe interna para ranquear candidatos de título
     */
    private static class TitleCandidate {
        private final String text;
        private final int score;

        public TitleCandidate(String text, int score) {
            this.text = text;
            this.score = score;
        }

        public String getText() {
            return text;
        }

        public int getScore() {
            return score;
        }

        @Override
        public String toString() {
            return text + " (score: " + score + ")";
        }
    }
}
