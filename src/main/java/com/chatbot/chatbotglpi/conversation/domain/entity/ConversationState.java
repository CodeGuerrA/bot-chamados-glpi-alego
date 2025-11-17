package com.chatbot.chatbotglpi.conversation.domain.entity;

import com.chatbot.chatbotglpi.conversation.domain.enums.StateEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder//O Lombok gera uma classe interna chamada ConversationStateBuilder que permite criar objetos do tipo ConversationState de forma fluente
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // Ignora campos desconhecidos do JSON ao desserializar
public class ConversationState implements Serializable { // Representa o estado da conversa com o usuário

    // --------------------------------------------------------
    // Identificador de versão para serialização
    private static final long serialVersionUID = 1L;

    // --------------------------------------------------------
    // Número de telefone do usuário associado à conversa
    private String phone;

    // Estado atual da conversa (ex: GREETING, COLLECTING_TITLE, CONFIRMING, etc)
    private StateEnum currentState;
    //estado para retornar
    private StateEnum returnState;

    // Mapa para armazenar dados da conversa como título, descrição, categoria e urgência
    private Map<String, String> data;

    // Data/hora da última interação do usuário
    private LocalDateTime lastActivity;

    // Data/hora de início da conversa
    private LocalDateTime startedAt;

    // --------------------------------------------------------
    // Adiciona ou atualiza um dado no mapa da conversa
    public void addData(String key, String value) {
        // Se o mapa ainda não existir, cria um novo HashMap
        if (this.data == null) this.data = new HashMap<>();

        // Adiciona ou atualiza o valor associado à chave
        this.data.put(key, value);
    }
    // --------------------------------------------------------
    // Retorna o valor de um dado baseado na chave
    public String getData(String key) {
        // Retorna o valor se o mapa existir, senão retorna null
        return this.data != null ? this.data.get(key) : null;
    }

    // --------------------------------------------------------
    // Verifica se um dado específico existe no mapa
    public boolean hasData(String key) {
        // Retorna true se o mapa existir e conter a chave
        return this.data != null && this.data.containsKey(key);
    }

    // --------------------------------------------------------
    // Verifica se todos os dados obrigatórios foram preenchidos
    public boolean isComplete() {
        // Checa se título, descrição, categoria e urgência estão presentes
        return hasData("title") &&
                hasData("description") &&
                hasData("locate") &&
                hasData("ramal");
    }

    // --------------------------------------------------------
    // Atualiza a última atividade para a data/hora atual
    public void updateActivity() {
        this.lastActivity = LocalDateTime.now();
    }
}
