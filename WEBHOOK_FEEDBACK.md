# 🔔 Guia de Implementação - Webhook Feedback do GLPI

## Visão Geral

Este documento explica como configurar o webhook do GLPI para solicitar feedback automaticamente quando um chamado é fechado/resolvido.

---

## Fluxo Completo

```
1. Técnico resolve/fecha chamado no GLPI
        ↓
2. GLPI detecta mudança de status
        ↓
3. GLPI envia webhook para Chatbot
   POST /api/webhook/glpi
   Headers: X-Webhook-Signature (HMAC)
   Body: {ticket_id, status, title, user_phone}
        ↓
4. Chatbot valida assinatura HMAC
        ↓
5. Chatbot verifica idempotência (Redis)
        ↓
6. Chatbot envia mensagem via Evolution API:
   "⭐ Como foi o atendimento?
    Seu chamado #1234 foi finalizado..."
        ↓
7. Usuário responde com nota (1-5)
        ↓
8. Chatbot envia feedback para GLPI API
   POST /apirest.php/Ticket/1234/Satisfaction
        ↓
9. GLPI salva feedback no banco de dados
```

---

## Parte 1: Configuração do GLPI

### 1.1 Instalar Plugin de Webhooks

O GLPI não tem webhooks nativos. Você precisa instalar um plugin:

**Opção A: Plugin "Webhooks" (Recomendado)**

```bash
cd /var/www/html/glpi/plugins
git clone https://github.com/pluginsGLPI/webhooks.git
chown -R www-data:www-data webhooks/
```

No GLPI:
```
Setup → Plugins → Webhooks → Install → Enable
```

**Opção B: Custom Trigger (código próprio)**

Se preferir, crie um trigger customizado no banco do GLPI.

### 1.2 Configurar Webhook no Plugin

```
Setup → Dropdowns → Webhooks
→ Add new webhook

Nome: Chatbot Feedback
URL: https://seu-dominio.com/api/webhook/glpi
Método: POST
Headers:
  Content-Type: application/json
  X-Webhook-Signature: {signature}

Eventos:
  ☑ Ticket fechado
  ☑ Ticket resolvido

Filtros:
  Status: Solved, Closed
```

### 1.3 Gerar Chave Secreta Compartilhada

```bash
# Gere uma chave forte
openssl rand -hex 32

# Exemplo: a1b2c3d4e5f6...
```

**Configure no GLPI:**
```
Webhooks → Configurações → Secret Key
Cole a chave gerada
```

**Configure no Chatbot:**
```bash
# docker-compose.yml
environment:
  GLPI_WEBHOOK_SECRET: "a1b2c3d4e5f6..."  # Mesma chave!
```

### 1.4 Estrutura do Payload

O GLPI enviará este JSON:

```json
{
  "event": "ticket.solved",
  "timestamp": "2025-01-19T14:30:00Z",
  "data": {
    "ticket": {
      "id": 1234,
      "title": "Computador com tela preta",
      "status": 5,
      "status_name": "Solved",
      "requester": {
        "id": 42,
        "name": "Carlos Garcia",
        "email": "carlos@alego.go.gov.br",
        "phone": "5562999999999"
      },
      "assigned_tech": {
        "id": 10,
        "name": "João Técnico"
      },
      "solved_at": "2025-01-19T14:25:00Z",
      "created_at": "2025-01-19T10:00:00Z"
    }
  }
}
```

---

## Parte 2: Código do Chatbot (Webhook Receiver)

### 2.1 Controller de Webhook GLPI

O controller já existe, mas vamos entender seu funcionamento:

**Arquivo:** `GlpiWebhookController.java`

```java
@PostMapping
public ResponseEntity<String> handleWebhook(
        @RequestBody String rawPayload,
        @RequestHeader(value = "X-Webhook-Signature", required = false) String signature) {

    // 1. Valida assinatura HMAC
    if (securityConfig.isEnabled()) {
        if (!signatureValidator.validateSignature(rawPayload, signature, secret)) {
            return ResponseEntity.status(401).body("Invalid signature");
        }
    }

    // 2. Parse do JSON
    GlpiWebhookEvent event = objectMapper.readValue(rawPayload, GlpiWebhookEvent.class);

    // 3. Verifica idempotência
    String eventId = "glpi:" + event.getData().getTicket().getId() + ":" + event.getTimestamp();
    if (!idempotencyService.tryAcquire(eventId)) {
        return ResponseEntity.ok("Duplicate event ignored");
    }

    // 4. Processa evento
    glpiWebhookService.processTicketSolved(event);

    return ResponseEntity.ok("Processed");
}
```

### 2.2 Serviço de Processamento

**Arquivo:** `GlpiWebhookService.java`

```java
@Service
public class GlpiWebhookService {

    public void processTicketSolved(GlpiWebhookEvent event) {
        Ticket ticket = event.getData().getTicket();

        // Extrai telefone do usuário
        String phone = ticket.getRequester().getPhone();
        if (phone == null || phone.isBlank()) {
            log.warn("Ticket #{} sem telefone - feedback não enviado", ticket.getId());
            return;
        }

        // Monta mensagem de feedback
        String message = feedbackService.buildFeedbackRequestMessage(
            ticket.getId(),
            ticket.getTitle()
        );

        // Envia via Evolution API
        evolutionService.sendMessage(phone, message);

        log.info("Feedback solicitado para ticket #{} - Usuário: {}",
                 ticket.getId(), phone);
    }
}
```

### 2.3 DTOs Necessários

**Arquivo:** `GlpiWebhookEvent.java`

```java
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GlpiWebhookEvent {
    private String event;           // "ticket.solved"
    private String timestamp;       // ISO 8601
    private Data data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {
        private Ticket ticket;

        @lombok.Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Ticket {
            private Long id;
            private String title;
            private Integer status;
            @JsonProperty("status_name")
            private String statusName;
            private Requester requester;
            @JsonProperty("solved_at")
            private String solvedAt;

            @lombok.Data
            public static class Requester {
                private Long id;
                private String name;
                private String email;
                private String phone;  // ← Campo crítico!
            }
        }
    }
}
```

---

## Parte 3: Fluxo de Feedback (Usuário Responde)

### 3.1 Usuário Recebe Mensagem

```
⭐ Como foi o atendimento?

Seu chamado #1234 foi finalizado:
📋 Computador com tela preta

Por favor, avalie nosso atendimento de 1 a 5 estrelas:

1️⃣ = Muito ruim
2️⃣ = Ruim
3️⃣ = Regular
4️⃣ = Bom
5️⃣ = Excelente

Digite apenas o número da sua avaliação.
```

### 3.2 Usuário Responde

```
Usuário: 5
```

### 3.3 Chatbot Processa Resposta

O webhook da Evolution API detecta a mensagem e processa:

```java
// ProcessMessageUseCase ou similar
if (isFeedbackContext(phone)) {
    // Valida nota (1-5)
    if (feedbackService.isValidRating(message)) {
        // Cria objeto de feedback
        TicketFeedback feedback = feedbackService.processFeedback(
            ticketId,
            phone,
            message,  // "5"
            null      // Comentário opcional
        );

        // Envia para GLPI API
        glpiClient.sendFeedback(feedback);

        // Responde ao usuário
        return feedbackService.buildFeedbackThankYouMessage(5, Optional.empty());
    }
}
```

### 3.4 Chatbot Envia Feedback ao GLPI

**Arquivo:** `GlpiClient.java` (novo método)

```java
@CircuitBreaker(name = "glpi", fallbackMethod = "sendFeedbackFallback")
public void sendFeedback(TicketFeedback feedback) {
    String sessionToken = sessionManager.getSessionToken();

    // Monta payload de satisfação do GLPI
    Map<String, Object> payload = Map.of(
        "tickets_id", feedback.getTicketId(),
        "satisfaction", feedback.getRating(),  // 1-5
        "comment", feedback.getComment() != null ? feedback.getComment() : ""
    );

    HttpHeaders headers = new HttpHeaders();
    headers.set("Session-Token", sessionToken);
    headers.set("App-Token", appToken);

    HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

    // POST /apirest.php/Ticket/{id}/Satisfaction
    String url = glpiApiUrl + "/Ticket/" + feedback.getTicketId() + "/Satisfaction";

    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    if (response.getStatusCode().is2xxSuccessful()) {
        log.info("Feedback enviado ao GLPI - Ticket #{}", feedback.getTicketId());
    }
}
```

---

## Parte 4: Configuração Completa Passo a Passo

### Passo 1: Gerar Chave Secreta

```bash
openssl rand -hex 32
# Resultado: a1b2c3d4e5f6789...
```

### Passo 2: Configurar GLPI

```
1. Instalar plugin Webhooks
2. Setup → Dropdowns → Webhooks → Add
   - Nome: Chatbot Feedback
   - URL: https://chatbot.alego.com/api/webhook/glpi
   - Secret: a1b2c3d4e5f6789...
   - Eventos: ticket.solved, ticket.closed
```

### Passo 3: Configurar Chatbot

```yaml
# docker-compose.yml
environment:
  GLPI_WEBHOOK_SECRET: "a1b2c3d4e5f6789..."
  WEBHOOK_SECURITY_ENABLED: "true"
```

### Passo 4: Testar Configuração

```bash
# 1. Simule webhook do GLPI
PAYLOAD='{"event":"ticket.solved","data":{"ticket":{"id":1234,"phone":"5562999999999"}}}'
SECRET="a1b2c3d4e5f6789..."

# Calcula assinatura
SIGNATURE=$(echo -n "$PAYLOAD" | openssl dgst -sha256 -hmac "$SECRET" | awk '{print $2}')

# Envia webhook
curl -X POST http://localhost:8082/api/webhook/glpi \
  -H "Content-Type: application/json" \
  -H "X-Webhook-Signature: $SIGNATURE" \
  -d "$PAYLOAD"

# Esperado: 200 OK - Usuário recebe mensagem no WhatsApp
```

### Passo 5: Verificar Logs

```bash
# Logs do Chatbot
docker-compose logs -f chatbot-app | grep "Feedback solicitado"

# Esperado:
# INFO GlpiWebhookService - Feedback solicitado para ticket #1234 - Usuário: 5562999999999
```

---

## Parte 5: Cenários e Edge Cases

### Cenário 1: Ticket sem Telefone

**Problema:** Usuário não tem telefone cadastrado no GLPI

**Solução:**
```java
if (phone == null || phone.isBlank()) {
    log.warn("Ticket #{} sem telefone - ignorando", ticket.getId());
    // NÃO envia feedback
    return;
}
```

### Cenário 2: Webhook Duplicado

**Problema:** GLPI envia webhook 2x

**Solução:** Idempotência automática
```java
String eventId = "glpi:" + ticketId + ":" + timestamp;
if (!idempotencyService.tryAcquire(eventId)) {
    return "Duplicate ignored";
}
```

### Cenário 3: Usuário Não Responde

**Problema:** Usuário ignora mensagem de feedback

**Solução:** Timeout após 24h
```java
// Armazena contexto de feedback no Redis com TTL
redisTemplate.opsForValue().set(
    "feedback:" + phone,
    ticketId,
    Duration.ofHours(24)
);
```

### Cenário 4: Formato de Telefone Incorreto

**Problema:** Telefone no GLPI: "(62) 99999-9999"

**Solução:** Normalização automática
```java
public String normalizePhone(String phone) {
    // Remove tudo exceto dígitos
    String digits = phone.replaceAll("[^0-9]", "");

    // Adiciona código do país se necessário
    if (digits.length() == 11) {  // DDD + número
        return "55" + digits;  // Brasil
    }
    return digits;
}
```

---

## Parte 6: Monitoramento e Troubleshooting

### Métricas Importantes

```
# Total de feedbacks solicitados
chatbot_feedback_requests_total

# Total de feedbacks recebidos
chatbot_feedback_received_total

# Taxa de resposta de feedback
chatbot_feedback_response_rate

# Média de rating
chatbot_feedback_average_rating
```

### Logs para Debug

```bash
# Webhook GLPI recebido
grep "Webhook GLPI recebido" logs/application.log

# Assinatura validada
grep "Assinatura HMAC validada" logs/application.log

# Feedback solicitado
grep "Feedback solicitado" logs/application.log

# Feedback enviado ao GLPI
grep "Feedback enviado ao GLPI" logs/application.log
```

### Troubleshooting Comum

| Problema | Causa | Solução |
|----------|-------|---------|
| Webhook retorna 401 | Assinatura inválida | Verifique secrets no GLPI e Chatbot |
| Usuário não recebe mensagem | Telefone incorreto/ausente | Valide campo phone no GLPI |
| Feedback não chega no GLPI | API Token inválido | Verifique credenciais GLPI |
| Mensagem duplicada | Idempotência falhou | Verifique Redis está rodando |

---

## Parte 7: Exemplo Completo End-to-End

### 1. Criar Ticket no GLPI

```
Usuário: Carlos Garcia
Telefone: 5562999999999
Problema: Computador com tela preta
```

### 2. Técnico Resolve

```sql
-- Banco GLPI
UPDATE glpi_tickets
SET status = 5  -- Solved
WHERE id = 1234;
```

### 3. GLPI Envia Webhook

```http
POST https://chatbot.alego.com/api/webhook/glpi
Headers:
  Content-Type: application/json
  X-Webhook-Signature: abc123...

Body:
{
  "event": "ticket.solved",
  "timestamp": "2025-01-19T14:30:00Z",
  "data": {
    "ticket": {
      "id": 1234,
      "title": "Computador com tela preta",
      "requester": {
        "phone": "5562999999999"
      }
    }
  }
}
```

### 4. Chatbot Valida e Envia Mensagem

```
WhatsApp → Carlos Garcia:

⭐ Como foi o atendimento?

Seu chamado #1234 foi finalizado:
📋 Computador com tela preta

Por favor, avalie de 1 a 5 estrelas...
```

### 5. Usuário Responde

```
Carlos Garcia → Chatbot: 5
```

### 6. Chatbot Envia ao GLPI

```http
POST https://glpi.alego.com/apirest.php/Ticket/1234/Satisfaction
Headers:
  Session-Token: xyz789...
  App-Token: abc123...

Body:
{
  "tickets_id": 1234,
  "satisfaction": 5,
  "comment": ""
}
```

### 7. Usuário Recebe Confirmação

```
🎉 Obrigado pela sua avaliação!

Você avaliou nosso atendimento com: ⭐⭐⭐⭐⭐ (5/5)

Sua opinião é muito importante!

💬 Precisa de ajuda novamente?
Digite oi para abrir um novo chamado
```

---

## Resumo - Checklist de Implementação

- [ ] Instalar plugin Webhooks no GLPI
- [ ] Gerar chave secreta compartilhada (32+ chars)
- [ ] Configurar webhook no GLPI (URL, secret, eventos)
- [ ] Configurar variável GLPI_WEBHOOK_SECRET no Chatbot
- [ ] Implementar GlpiWebhookController (receber webhook)
- [ ] Implementar GlpiWebhookService (processar evento)
- [ ] Adicionar DTOs (GlpiWebhookEvent)
- [ ] Implementar GlpiClient.sendFeedback() (enviar ao GLPI)
- [ ] Testar com webhook simulado
- [ ] Testar com ticket real do GLPI
- [ ] Configurar métricas de feedback
- [ ] Configurar alertas para falhas
- [ ] Documentar fluxo para equipe de suporte

---

## Referências

- [GLPI API Documentation](https://github.com/glpi-project/glpi/blob/master/apirest.md)
- [Plugin Webhooks GLPI](https://github.com/pluginsGLPI/webhooks)
- [SEGURANCA_WEBHOOKS.md](SEGURANCA_WEBHOOKS.md) - Validação HMAC

---

**Desenvolvido pela Equipe de TI da ALEGO**
