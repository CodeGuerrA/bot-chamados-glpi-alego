# Guia de Segurança de Webhooks

## Visão Geral

Este documento explica como configurar e usar a segurança de webhooks implementada no chatbot.

## Funcionalidades Implementadas

### 1. Validação de Assinatura HMAC-SHA256

Todos os webhooks agora validam assinatura criptográfica para garantir autenticidade.

**Benefícios:**
- ✅ Previne falsificação de webhooks
- ✅ Garante que requisições vêm da Evolution API/GLPI
- ✅ Protege contra ataques man-in-the-middle
- ✅ Constant-time comparison (previne timing attacks)

### 2. Idempotência

Sistema de deduplicação baseado em Redis previne processamento duplicado.

**Benefícios:**
- ✅ Previne tickets duplicados
- ✅ Garante exactly-once semantics
- ✅ Cache com TTL automático (24h padrão)
- ✅ Operação atômica (SET NX EX)

### 3. UX Melhorada

Mensagem de sucesso reformulada com informações claras e organizadas.

---

## Configuração

### Passo 1: Gerar Chaves Secretas

Gere chaves secretas fortes (recomendado 32+ caracteres):

```bash
# Linux/Mac
openssl rand -hex 32

# Ou use um gerador online confiável
# https://www.random.org/strings/
```

### Passo 2: Configurar Variáveis de Ambiente

**Em Desenvolvimento (docker-compose.yml):**

```yaml
services:
  chatbot-app:
    environment:
      # Segurança de Webhooks
      EVOLUTION_WEBHOOK_SECRET: "sua-chave-secreta-evolution-aqui-32-chars-minimo"
      GLPI_WEBHOOK_SECRET: "sua-chave-secreta-glpi-aqui-32-chars-minimo"
      WEBHOOK_SECURITY_ENABLED: "true"  # NUNCA desabilite em produção!
```

**Em Produção (Kubernetes, variáveis de ambiente, etc):**

```bash
export EVOLUTION_WEBHOOK_SECRET="sua-chave-super-secreta-evolution"
export GLPI_WEBHOOK_SECRET="sua-chave-super-secreta-glpi"
export WEBHOOK_SECURITY_ENABLED="true"
```

### Passo 3: Configurar Evolution API

Configure a Evolution API para enviar assinatura HMAC nos webhooks.

**Configuração da Evolution API:**

1. Acesse as configurações da instância
2. Configure webhook URL: `https://seu-dominio.com/api/webhook/evolution`
3. Adicione header customizado:
   - **Nome:** `X-Webhook-Signature`
   - **Valor:** `{signature}` (calculado com HMAC-SHA256)

**Exemplo de código Evolution API (se você controlar):**

```javascript
const crypto = require('crypto');

function calculateSignature(payload, secret) {
  return crypto
    .createHmac('sha256', secret)
    .update(JSON.stringify(payload))
    .digest('hex');
}

// Ao enviar webhook
const signature = calculateSignature(webhookPayload, 'sua-chave-secreta');

axios.post('https://chatbot.com/api/webhook/evolution', webhookPayload, {
  headers: {
    'X-Webhook-Signature': signature,
    'Content-Type': 'application/json'
  }
});
```

### Passo 4: Testar a Configuração

**Teste Positivo (assinatura válida):**

```bash
#!/bin/bash

PAYLOAD='{"event":"messages.upsert","data":{"key":{"id":"ABC123"}}}'
SECRET="sua-chave-secreta-evolution"

# Calcula assinatura
SIGNATURE=$(echo -n "$PAYLOAD" | openssl dgst -sha256 -hmac "$SECRET" | awk '{print $2}')

# Envia webhook
curl -X POST http://localhost:8082/api/webhook/evolution \
  -H "Content-Type: application/json" \
  -H "X-Webhook-Signature: $SIGNATURE" \
  -d "$PAYLOAD"

# Esperado: 200 OK
```

**Teste Negativo (assinatura inválida):**

```bash
curl -X POST http://localhost:8082/api/webhook/evolution \
  -H "Content-Type: application/json" \
  -H "X-Webhook-Signature: assinatura-invalida" \
  -d '{"event":"messages.upsert"}'

# Esperado: 401 Unauthorized - Invalid webhook signature
```

**Teste de Idempotência:**

```bash
# Envia a mesma mensagem 2x (mesmo messageId)
# Primeira vez: processa normalmente
# Segunda vez: retorna "Duplicate message ignored"
```

---

## Como Funciona

### Fluxo de Validação

```
┌─────────────────────┐
│ Evolution API       │
│ envia webhook       │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────────────────────────┐
│ 1. Extrai header X-Webhook-Signature    │
└──────────┬──────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────┐
│ 2. Calcula HMAC-SHA256 do payload       │
│    usando chave secreta                 │
└──────────┬──────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────┐
│ 3. Compara assinaturas (constant-time)  │
└──────────┬──────────────────────────────┘
           │
      ┌────┴────┐
      │         │
   INVÁLIDA  VÁLIDA
      │         │
      ▼         ▼
  ┌─────┐   ┌──────────────────────┐
  │ 401 │   │ 4. Verifica          │
  │ DENY│   │    idempotência      │
  └─────┘   │    no Redis          │
            └──────────┬───────────┘
                       │
                  ┌────┴────┐
                  │         │
              DUPLICADA   NOVA
                  │         │
                  ▼         ▼
            ┌─────────┐  ┌──────────┐
            │ 200 OK  │  │ PROCESSA │
            │ IGNORE  │  │ MENSAGEM │
            └─────────┘  └──────────┘
```

### Algoritmo HMAC-SHA256

```java
// 1. Chatbot e Evolution API compartilham chave secreta (nunca trafega na rede)
String secret = "chave-super-secreta-compartilhada";

// 2. Evolution API calcula assinatura antes de enviar
String signature = HMAC_SHA256(payload, secret);

// 3. Evolution API envia payload + assinatura
POST /webhook
Headers: X-Webhook-Signature: {signature}
Body: {payload}

// 4. Chatbot recebe e recalcula assinatura
String expectedSignature = HMAC_SHA256(receivedPayload, secret);

// 5. Compara de forma segura (constant-time)
if (expectedSignature == receivedSignature) {
    // Autenticado! Processa webhook
} else {
    // Rejeitado! Possível ataque
}
```

---

## Monitoramento

### Logs de Segurança

**Assinatura válida:**
```
DEBUG WebhookSignatureValidator - Assinatura HMAC validada com sucesso
```

**Assinatura inválida:**
```
WARN  EvolutionWebhookController - Webhook Evolution sem assinatura - rejeitado
ERROR WebhookSignatureValidator - Assinatura inválida. Esperado: 1a2b3c4d..., Recebido: 9z8y7x6w...
```

**Mensagem duplicada (idempotência):**
```
INFO  IdempotencyService - Operação duplicada detectada: webhook:evolution:ABC123XYZ
INFO  EvolutionWebhookController - Mensagem duplicada detectada: ABC123XYZ - ignorando
```

### Métricas Prometheus

Adicione estas métricas ao `BotMetrics`:

```java
// Sugestão de novas métricas
Counter webhookSignatureInvalid = Counter.builder("chatbot_webhook_signature_invalid_total")
    .description("Total de webhooks rejeitados por assinatura inválida")
    .register(registry);

Counter webhookDuplicates = Counter.builder("chatbot_webhook_duplicates_total")
    .description("Total de webhooks duplicados ignorados")
    .register(registry);
```

---

## Troubleshooting

### Problema: Webhook sempre retorna 401 Unauthorized

**Causa:** Assinatura inválida

**Soluções:**
1. Verifique se a chave secreta está correta em AMBOS os lados
2. Confirme que Evolution API está calculando HMAC-SHA256 corretamente
3. Certifique-se de usar o payload RAW (não formatado)
4. Verifique encoding UTF-8 em ambos os lados

### Problema: Mensagens legítimas sendo marcadas como duplicadas

**Causa:** MessageId duplicado ou TTL muito longo

**Soluções:**
1. Verifique se Evolution API está gerando IDs únicos
2. Reduza TTL de idempotência (padrão: 24h)
3. Limpe cache manualmente: `idempotencyService.remove("webhook:evolution:ID")`

### Problema: Warnings "Validação de webhook desabilitada"

**Causa:** `WEBHOOK_SECURITY_ENABLED=false`

**Solução:**
```bash
# NUNCA desabilite em produção!
export WEBHOOK_SECURITY_ENABLED=true
```

---

## Segurança em Profundidade

### Outras Recomendações

1. **HTTPS Obrigatório**
   - Use TLS 1.2+ em produção
   - Webhooks devem ser HTTPS only

2. **Rate Limiting**
   - Implemente rate limiting por IP
   - Use Resilience4j `@RateLimiter`

3. **IP Whitelist** (opcional)
   ```java
   @Component
   public class WebhookIpWhitelistFilter implements Filter {
       private static final Set<String> ALLOWED_IPS = Set.of(
           "192.168.1.100",  // Evolution API
           "10.0.0.50"       // GLPI
       );
   }
   ```

4. **Rotação de Secrets**
   - Troque chaves secretas a cada 90 dias
   - Use secrets manager (Vault, AWS Secrets)

5. **Auditoria**
   - Log todas as tentativas de webhook
   - Alerte sobre múltiplas falhas de autenticação

---

## Exemplo Completo de Integração

### Código da Evolution API (Node.js)

```javascript
const crypto = require('crypto');
const axios = require('axios');

class ChatbotWebhookClient {
  constructor(webhookUrl, secret) {
    this.webhookUrl = webhookUrl;
    this.secret = secret;
  }

  calculateSignature(payload) {
    const payloadString = JSON.stringify(payload);
    return crypto
      .createHmac('sha256', this.secret)
      .update(payloadString)
      .digest('hex');
  }

  async sendWebhook(event) {
    const payload = {
      event: event.type,
      instance: 'chatbot',
      data: event.data
    };

    const signature = this.calculateSignature(payload);

    try {
      const response = await axios.post(this.webhookUrl, payload, {
        headers: {
          'Content-Type': 'application/json',
          'X-Webhook-Signature': signature
        },
        timeout: 5000
      });

      console.log('Webhook enviado com sucesso:', response.status);
      return response.data;

    } catch (error) {
      if (error.response?.status === 401) {
        console.error('Webhook rejeitado: assinatura inválida');
      } else if (error.response?.status === 200 &&
                 error.response?.data === 'Duplicate message ignored') {
        console.log('Mensagem duplicada ignorada (idempotência OK)');
      } else {
        console.error('Erro ao enviar webhook:', error.message);
      }
      throw error;
    }
  }
}

// Uso
const client = new ChatbotWebhookClient(
  'https://chatbot.alego.com/api/webhook/evolution',
  process.env.WEBHOOK_SECRET
);

// Quando receber mensagem do WhatsApp
evolutionApi.on('message', async (message) => {
  await client.sendWebhook({
    type: 'messages.upsert',
    data: {
      key: {
        id: message.id,
        remoteJid: message.from,
        fromMe: false
      },
      message: {
        conversation: message.text
      }
    }
  });
});
```

---

## Referências

- [HMAC RFC 2104](https://tools.ietf.org/html/rfc2104)
- [Webhook Security Best Practices](https://webhooks.fyi/security/hmac)
- [OWASP API Security](https://owasp.org/www-project-api-security/)

---

## Checklist de Deployment

- [ ] Chaves secretas geradas (32+ caracteres)
- [ ] Variáveis de ambiente configuradas
- [ ] Evolution API configurada para enviar assinatura
- [ ] Testes de validação executados com sucesso
- [ ] Testes de idempotência verificados
- [ ] HTTPS habilitado em produção
- [ ] Logs de segurança monitorados
- [ ] Alertas configurados para falhas de autenticação
- [ ] Documentação atualizada para time de ops
- [ ] Secrets rotacionados em produção (pós-deploy)
