# Guia de Rate Limiting

## Visão Geral

Sistema de proteção contra abuso de endpoints implementado usando Resilience4j.

**Proteção por IP** - cada endereço IP tem seu próprio limite de requisições.

---

## Limites Configurados

### Ambiente de Desenvolvimento

| Endpoint | Limite | Período | Req/Segundo |
|----------|--------|---------|-------------|
| `/api/webhook/**` | 30 req | 60s | ~0.5/s |
| `/api/**` (geral) | 100 req | 60s | ~1.6/s |
| `/actuator/health` | 10 req | 60s | ~0.16/s |

### Ambiente de Produção (Mais Rigoroso)

| Endpoint | Limite | Período | Req/Segundo |
|----------|--------|---------|-------------|
| `/api/webhook/**` | 20 req | 60s | ~0.33/s |
| `/api/**` (geral) | 60 req | 60s | ~1/s |
| `/actuator/health` | 5 req | 60s | ~0.08/s |

---

## Como Funciona

### Detecção de IP Real

O sistema detecta o IP real do cliente mesmo através de proxies/load balancers:

```
Cliente → Nginx → Load Balancer → Chatbot
  ↓         ↓           ↓
  IP    X-Forwarded-For  ✓ IP detectado
```

**Headers verificados (em ordem):**
1. `X-Forwarded-For`
2. `X-Real-IP`
3. `Proxy-Client-IP`
4. `WL-Proxy-Client-IP`
5. Fallback: `request.getRemoteAddr()`

### Fluxo de Validação

```
Requisição HTTP
    ↓
Extrai IP do cliente (considerando proxies)
    ↓
Determina qual rate limiter usar (webhook/default/health)
    ↓
Chave: "webhook:192.168.1.100"
    ↓
RateLimiter tenta adquirir permissão
    ↓
┌─────────────┬──────────────┐
│ PERMITIDO   │   BLOQUEADO  │
│ (< limite)  │  (> limite)  │
└─────┬───────┴──────┬───────┘
      │              │
   200 OK        429 Too Many Requests
                     │
            {
              "error": "Too many requests",
              "message": "Rate limit exceeded...",
              "clientIp": "192.168.1.100"
            }
```

---

## Resposta ao Rate Limit Excedido

### HTTP Status
```
429 Too Many Requests
```

### Body (JSON)
```json
{
  "error": "Too many requests",
  "message": "Rate limit exceeded. Please try again later.",
  "clientIp": "192.168.1.100"
}
```

### Headers (futura implementação)
```
X-RateLimit-Limit: 30
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1637683200
Retry-After: 45
```

---

## Testes

### Teste Básico de Rate Limit

```bash
#!/bin/bash

# Envia 35 requisições rapidamente (limite: 30/min)
for i in {1..35}; do
  echo "Requisição $i:"
  curl -X POST http://localhost:8082/api/webhook/evolution/health \
       -H "Content-Type: application/json" \
       -w "\nStatus: %{http_code}\n\n"
  sleep 0.5
done

# Esperado:
# - Primeiras 30: 200 OK
# - 31-35: 429 Too Many Requests
```

### Teste com IPs Diferentes

```bash
# IP 1 - deve ter seu próprio contador
curl -X GET http://localhost:8082/api/webhook/evolution/health \
     -H "X-Forwarded-For: 192.168.1.100"

# IP 2 - contador independente
curl -X GET http://localhost:8082/api/webhook/evolution/health \
     -H "X-Forwarded-For: 192.168.1.200"

# Cada IP pode fazer até 10 requisições/min independentemente
```

### Verificar Logs

```bash
# Rate limit excedido
tail -f logs/application.log | grep "Rate limit excedido"

# Exemplo de log:
# WARN RateLimitInterceptor - Rate limit excedido - IP: 192.168.1.100 | Path: /api/webhook/evolution | Limiter: webhook
```

---

## Configuração

### Ajustar Limites

**Desenvolvimento** (`application.properties`):
```properties
# Webhooks: 30 req/min
resilience4j.ratelimiter.instances.webhook.limit-for-period=30
resilience4j.ratelimiter.instances.webhook.limit-refresh-period=60s
```

**Produção** (`application-prod.properties`):
```properties
# Webhooks: 20 req/min (mais rigoroso)
resilience4j.ratelimiter.instances.webhook.limit-for-period=20
resilience4j.ratelimiter.instances.webhook.limit-refresh-period=60s
```

### Criar Novo Rate Limiter

1. Adicionar configuração:
```properties
resilience4j.ratelimiter.instances.meu-endpoint.limit-for-period=50
resilience4j.ratelimiter.instances.meu-endpoint.limit-refresh-period=60s
resilience4j.ratelimiter.instances.meu-endpoint.timeout-duration=0s
```

2. Atualizar `RateLimitInterceptor.getRateLimiterName()`:
```java
private String getRateLimiterName(String path) {
    if (path.startsWith("/api/webhook/")) {
        return "webhook";
    } else if (path.startsWith("/api/meu-endpoint/")) {
        return "meu-endpoint";  // Novo!
    } else {
        return "default";
    }
}
```

---

## Monitoramento

### Métricas Prometheus

```
# Total de requisições bloqueadas por rate limit
http_server_requests_seconds_count{status="429"}

# Rate limiter atual
resilience4j_ratelimiter_available_permissions{name="webhook"}
```

### Dashboard Grafana (Sugestão)

```
Panel 1: Rate Limit - Requisições Bloqueadas
Query: rate(http_server_requests_seconds_count{status="429"}[5m])

Panel 2: Permissões Disponíveis por Limiter
Query: resilience4j_ratelimiter_available_permissions

Panel 3: Top IPs Bloqueados
Query: Parse logs → Count by IP
```

### Alertas Recomendados

```yaml
# Prometheus Alert Rules
groups:
  - name: rate_limiting
    rules:
      # Alerta se 10% das requisições estão sendo bloqueadas
      - alert: HighRateLimitBlocking
        expr: rate(http_server_requests_seconds_count{status="429"}[5m]) > 10
        for: 5m
        annotations:
          summary: "Taxa alta de bloqueio por rate limit"

      # Alerta se mesmo IP está sendo bloqueado repetidamente
      - alert: SuspiciousIPActivity
        expr: count_over_time({job="chatbot"}[10m]) > 100
        annotations:
          summary: "IP suspeito fazendo muitas requisições"
```

---

## Bypass de Rate Limit (Admin)

### IP Whitelist (Futuro)

```java
@Component
public class RateLimitInterceptor {

    // IPs que não sofrem rate limit (CI/CD, monitoramento)
    private static final Set<String> WHITELIST = Set.of(
        "10.0.0.1",      // Jenkins
        "10.0.0.2",      // Grafana
        "192.168.1.50"   // Monitoring server
    );

    @Override
    public boolean preHandle(...) {
        String ip = getClientIp(request);

        // Bypass para IPs na whitelist
        if (WHITELIST.contains(ip)) {
            return true;
        }

        // Aplica rate limit normalmente
        // ...
    }
}
```

### API Key com Limite Maior

```java
// Usuários com API key têm limite maior
if (request.getHeader("X-API-Key") != null) {
    rateLimiterName = "authenticated"; // 1000 req/min
} else {
    rateLimiterName = "default"; // 100 req/min
}
```

---

## Troubleshooting

### Problema: Requisições legítimas sendo bloqueadas

**Causa:** Limite muito baixo ou múltiplos usuários atrás do mesmo IP (NAT)

**Solução:**
1. Aumentar limite para aquele endpoint
2. Implementar autenticação para limites individuais
3. Adicionar IP na whitelist (se confiável)

### Problema: Rate limit não está funcionando

**Verificar:**
1. Interceptor está registrado?
```bash
curl http://localhost:8082/actuator/mappings | grep RateLimitInterceptor
```

2. Configuração está carregada?
```bash
curl http://localhost:8082/actuator/configprops | grep ratelimiter
```

3. Logs mostram aplicação do interceptor?
```bash
tail -f logs/application.log | grep RateLimitInterceptor
```

### Problema: Mesmo IP aparece como IPs diferentes

**Causa:** Proxy/Load balancer não está enviando header `X-Forwarded-For`

**Solução:**
```nginx
# Nginx
proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
proxy_set_header X-Real-IP $remote_addr;
```

---

## Comparação com Outras Soluções

| Solução | Onde Aplica | Vantagem | Desvantagem |
|---------|-------------|----------|-------------|
| **RateLimitInterceptor** | App (Spring) | Granular, por IP | Não distribui entre instâncias |
| API Gateway | Gateway | Distribui carga | Configuração separada |
| Nginx | Reverse Proxy | Performance | Menos flexível |
| Redis | Centralizado | Multi-instância | Latência extra |

### Solução Híbrida (Recomendado)

```
┌─────────────────────────────────────┐
│ 1. Nginx (1000 req/s global)       │
├─────────────────────────────────────┤
│ 2. API Gateway (500 req/s por rota)│
├─────────────────────────────────────┤
│ 3. RateLimitInterceptor             │
│    (100 req/min por IP)             │
└─────────────────────────────────────┘
```

---

## Evolução Futura

### Fase 1 - Atual ✅
- [x] Rate limiting por IP
- [x] Configuração por endpoint
- [x] Logs de bloqueio

### Fase 2 - Próximos Passos
- [ ] Headers de rate limit (X-RateLimit-*)
- [ ] IP Whitelist configurável
- [ ] Métricas Prometheus detalhadas
- [ ] Dashboard Grafana

### Fase 3 - Avançado
- [ ] Rate limiting distribuído (Redis)
- [ ] Limites por usuário autenticado
- [ ] Throttling dinâmico (aumenta em horários de pico)
- [ ] Machine learning para detectar padrões de abuso

---

## Referências

- [Resilience4j Documentation](https://resilience4j.readme.io/docs/ratelimiter)
- [HTTP 429 Too Many Requests](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/429)
- [OWASP Rate Limiting](https://cheatsheetseries.owasp.org/cheatsheets/Denial_of_Service_Cheat_Sheet.html)

---

## Checklist de Deployment

- [ ] Limites configurados adequadamente para produção
- [ ] Testes de rate limit executados
- [ ] Monitoramento configurado
- [ ] Alertas de bloqueio configurados
- [ ] Documentação atualizada
- [ ] Logs de rate limit sendo coletados
- [ ] Dashboards criados
- [ ] Whitelist de IPs confiáveis configurada (se aplicável)
