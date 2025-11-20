package com.chatbot.chatbotglpi.integration.evolution.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Deserializador customizado para o campo 'data' do WebhookEvent.
 * Aceita tanto um objeto único quanto um array de objetos.
 */
public class DataListDeserializer extends JsonDeserializer<List<WebhookEvent.Data>> {

    @Override
    public List<WebhookEvent.Data> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        List<WebhookEvent.Data> dataList = new ArrayList<>();
        ObjectMapper mapper = (ObjectMapper) parser.getCodec();

        // Verifica se é um array ou objeto único
        if (parser.currentToken() == JsonToken.START_ARRAY) {
            // É um array - deserializa normalmente
            while (parser.nextToken() != JsonToken.END_ARRAY) {
                WebhookEvent.Data data = mapper.readValue(parser, WebhookEvent.Data.class);
                dataList.add(data);
            }
        } else if (parser.currentToken() == JsonToken.START_OBJECT) {
            // É um objeto único - converte para lista com um elemento
            WebhookEvent.Data data = mapper.readValue(parser, WebhookEvent.Data.class);
            dataList.add(data);
        }

        return dataList;
    }
}
