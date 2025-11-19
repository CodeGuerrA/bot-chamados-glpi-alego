# API Gateway - Chatbot GLPI

API Gateway para o sistema de Chatbot GLPI utilizando Spring Cloud Gateway.

## Funcionalidades

✅ **Roteamento Inteligente**: Direciona requisições para os serviços corretos
✅ **Rate Limiting**: Protege contra abuso com limite de requisições
✅ **Circuit Breaker**: Previne falhas em cascata
✅ **Retry**: Retenta automaticamente requisições falhadas
✅ **CORS**: Configuração global de CORS
✅ **Logging**: Logs detalhados de todas as requisições
✅ **Fallback**: Respostas amigáveis quando serviços estão indisponíveis

## Arquitetura

```
┌─────────────┐
│   Clients   │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────────┐
│         API Gateway                 │
│  - Rate Limiting (Redis)            │
│  - Circuit Breaker                  │
│  - Retry Logic                      │
│  - Logging                          │
└──────┬──────────────────────────────┘
       │
       ├──────────────────┬───────────────────┐
       ▼                  ▼                   ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  Chatbot    │    │  Evolution  │    │   GLPI      │
│  Service    │    │  Webhook    │    │  Service    │
│  (8082)     │    │             │    │             │
└─────────────┘    └─────────────┘    └─────────────┘
```

## Rotas

### Chatbot Service
- **Path**: `/api/chatbot/**`
- **Destino**: `http://localhost:8082`
- **Features**: Rate Limit, Circuit Breaker, Retry

### Evolution Webhook
- **Path**: `/webhook/**`
- **Destino**: `http://localhost:8082`
- **Features**: Circuit Breaker

### Actuator (Health Checks)
- **Path**: `/actuator/**`
- **Destino**: `http://localhost:8082`

## Configuração

### application.yml

As principais configurações estão em `src/main/resources/application.yml`:

- **Porta**: 8080
- **Rate Limit**: 10 requisições/segundo com burst de 20
- **Circuit Breaker**: 50% failure rate threshold
- **Timeout**: 10s para chatbot, 5s para webhook

### Variáveis de Ambiente

| Variável | Default | Descrição |
|----------|---------|-----------|
| `REDIS_HOST` | localhost | Host do Redis |
| `REDIS_PORT` | 6379 | Porta do Redis |
| `REDIS_PASSWORD` | (vazio) | Senha do Redis |

## Como Executar

### Requisitos

- Java 21+
- Maven 3.8+
- Redis (para Rate Limiter)

### Execução Local

```bash
# 1. Compile o projeto
mvn clean package

# 2. Execute
java -jar target/api-gateway-1.0.0.jar

# Ou com Maven
mvn spring-boot:run
```

### Com Docker Compose

```bash
docker-compose up -d
```

## Endpoints de Monitoramento

- **Health**: `GET http://localhost:8080/actuator/health`
- **Metrics**: `GET http://localhost:8080/actuator/metrics`
- **Gateway Routes**: `GET http://localhost:8080/actuator/gateway/routes`
- **Circuit Breakers**: Verificar estado via logs

## Testando

### Teste de Roteamento
```bash
curl -X POST http://localhost:8080/api/chatbot/webhook/evolution \
  -H "Content-Type: application/json" \
  -d '{"message": "Olá"}'
```

### Teste de Rate Limiting
```bash
# Execute múltiplas vezes rapidamente para atingir o limite
for i in {1..25}; do
  curl http://localhost:8080/api/chatbot/webhook/evolution
done
```

### Teste de Circuit Breaker
```bash
# Pare o serviço de chatbot e faça requisições
# O Gateway retornará fallback após algumas falhas
curl http://localhost:8080/api/chatbot/webhook/evolution
```

## Logs

Logs são exibidos no console com o formato:
```
2025-01-19 10:30:00 - Incoming request: POST /api/chatbot/webhook/evolution from /127.0.0.1:52134
2025-01-19 10:30:01 - Completed request: POST /api/chatbot/webhook/evolution - Status: 200 - Duration: 150ms
```

## Troubleshooting

### Rate Limit Sempre Ativado
- Verifique se o Redis está rodando
- Verifique a conexão com Redis

### Circuit Breaker Não Abre
- Aumente o número de requisições falhadas
- Verifique configuração `minimumNumberOfCalls`

### Timeout
- Aumente `timeoutDuration` em `application.yml`
- Verifique performance do serviço downstream

## Próximos Passos

- [ ] Adicionar autenticação JWT
- [ ] Implementar métricas customizadas
- [ ] Adicionar distributed tracing (Zipkin/Jaeger)
- [ ] Configurar HTTPS/TLS
- [ ] Implementar cache de respostas

## Contato

Para dúvidas ou sugestões, abra uma issue no repositório.
