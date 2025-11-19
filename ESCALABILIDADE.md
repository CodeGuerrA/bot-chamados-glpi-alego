# Guia de Escalabilidade do Chatbot

## Capacidade Atual

### Ambiente de Desenvolvimento
- Pool Redis: 8 conexões máximas
- Threads Tomcat: 200 (padrão)
- **Capacidade: ~100-150 usuários simultâneos**

### Ambiente de Produção
- Pool Redis: 20 conexões máximas
- Threads Tomcat: 200 (padrão)
- **Capacidade: ~300-500 usuários simultâneos**

## Como Aumentar a Capacidade

### 1. Aumentar Pool de Conexões Redis

#### Para 1.000 usuários simultâneos:
```properties
# application-prod.properties
spring.data.redis.lettuce.pool.max-active=50
spring.data.redis.lettuce.pool.max-idle=25
spring.data.redis.lettuce.pool.min-idle=10
```

#### Para 5.000 usuários simultâneos:
```properties
spring.data.redis.lettuce.pool.max-active=100
spring.data.redis.lettuce.pool.max-idle=50
spring.data.redis.lettuce.pool.min-idle=20
```

### 2. Aumentar Threads do Tomcat

Adicione em `application-prod.properties`:

```properties
# Para 1.000 usuários
server.tomcat.threads.max=400
server.tomcat.threads.min-spare=50
server.tomcat.accept-count=200

# Para 5.000 usuários
server.tomcat.threads.max=800
server.tomcat.threads.min-spare=100
server.tomcat.accept-count=500
```

### 3. Configurar Recursos da JVM

Aumente a memória heap no `docker-compose.yml`:

```yaml
environment:
  # Para 1.000 usuários
  JAVA_OPTS: "-Xms512m -Xmx2048m"

  # Para 5.000 usuários
  JAVA_OPTS: "-Xms1024m -Xmx4096m"
```

### 4. Redis Cluster (Para Produção de Alta Escala)

Se precisar suportar mais de 10.000 usuários:

```properties
# Use Redis Cluster ao invés de instância única
spring.redis.cluster.nodes=redis1:6379,redis2:6379,redis3:6379
spring.redis.cluster.max-redirects=3
spring.data.redis.lettuce.pool.max-active=200
```

### 5. Escalonamento Horizontal

Para escalabilidade ilimitada, use múltiplas instâncias:

```yaml
# docker-compose.yml
services:
  chatbot-app:
    deploy:
      replicas: 3  # 3 instâncias do chatbot
    # ... resto da configuração

  nginx:
    image: nginx:alpine
    # Configure load balancer
```

## Monitoramento de Capacidade

### Métricas Importantes

Acesse via Actuator: `http://localhost:8082/actuator/metrics`

1. **Conexões Redis ativas**:
   ```
   GET /actuator/metrics/lettuce.active.connections
   ```

2. **Threads Tomcat em uso**:
   ```
   GET /actuator/metrics/tomcat.threads.busy
   ```

3. **Conversas ativas no Redis**:
   ```bash
   redis-cli KEYS "conversation:*" | wc -l
   ```

### Alertas Recomendados

Configure alertas quando:
- Conexões Redis > 80% do máximo
- Threads Tomcat > 80% do máximo
- Tempo de resposta > 2 segundos

## Tabela de Referência Rápida

| Usuários Simultâneos | Pool Redis | Threads Tomcat | Memória JVM | Instâncias |
|---------------------|------------|----------------|-------------|------------|
| 100-500             | 20         | 200            | 1GB         | 1          |
| 500-1.000           | 50         | 400            | 2GB         | 1-2        |
| 1.000-5.000         | 100        | 800            | 4GB         | 2-3        |
| 5.000-10.000        | 200        | 1000           | 8GB         | 3-5        |
| 10.000+             | Cluster    | 1000           | 8GB         | 5+         |

## Notas Importantes

1. **Redis é o gargalo principal** - Priorize aumento do pool Redis
2. **APIs externas** (GLPI, Evolution) têm seus próprios limites
3. **Teste de carga** é essencial antes de ir para produção
4. **Monitore sempre** as métricas em produção
