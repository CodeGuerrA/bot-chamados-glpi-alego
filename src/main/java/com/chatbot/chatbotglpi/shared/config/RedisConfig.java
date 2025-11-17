package com.chatbot.chatbotglpi.shared.config;

import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

//para rodar os testes e so passar esse comando ./test_chatbot.sh.


@Configuration // Indica que esta classe é uma classe de configuração do Spring
public class RedisConfig {

    @Bean // Declara que o método retorna um bean gerenciado pelo Spring
    public RedisTemplate<String, ConversationState> redisTemplate(RedisConnectionFactory connectionFactory) {

        // Cria um ObjectMapper, responsável por converter objetos Java para JSON e vice-versa
        ObjectMapper mapper = new ObjectMapper();

        // Registra módulo que permite serializar/deserializar datas do Java 8 (LocalDateTime, etc.)
        mapper.registerModule(new JavaTimeModule());

        // Ativa tipagem polimórfica para que objetos complexos sejam corretamente serializados/deserializados
        mapper.activateDefaultTyping(
                mapper.getPolymorphicTypeValidator(), // Validador de tipos para segurança
                ObjectMapper.DefaultTyping.NON_FINAL // Aplica tipagem a todas classes não-final
        );

        // Cria um serializador JSON genérico para usar no Redis, usando o ObjectMapper configurado
        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(mapper);

        // Cria a instância do RedisTemplate que vai gerenciar objetos de ConversationState
        RedisTemplate<String, ConversationState> template = new RedisTemplate<>();

        // Associa o template à fábrica de conexões Redis
        template.setConnectionFactory(connectionFactory);
        // Configura o serializador das chaves como String (mais legível no Redis)
        template.setKeySerializer(new StringRedisSerializer());

        // Configura o serializador dos valores como JSON
        template.setValueSerializer(jsonSerializer);

        // Configura o serializador das chaves de hashes como String
        template.setHashKeySerializer(new StringRedisSerializer());

        // Configura o serializador dos valores de hashes como JSON
        template.setHashValueSerializer(jsonSerializer);

        // Inicializa o template após todas as propriedades terem sido configuradas
        template.afterPropertiesSet();

        // Retorna o template configurado para ser usado pelo Spring
        return template;
    }
}
