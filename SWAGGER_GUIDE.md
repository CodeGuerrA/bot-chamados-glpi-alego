# Guia de Uso do Swagger/OpenAPI

## Acessando a Documentação

Após iniciar a aplicação, você pode acessar a documentação interativa da API através dos seguintes URLs:

### Swagger UI (Interface Gráfica)
```
http://localhost:8082/swagger-ui.html
```

A interface Swagger UI permite:
- Visualizar todos os endpoints disponíveis
- Ver detalhes de cada endpoint (método HTTP, parâmetros, corpo da requisição)
- Testar endpoints diretamente pelo navegador
- Ver exemplos de requisições e respostas

### OpenAPI Specification (JSON)
```
http://localhost:8082/v3/api-docs
```

Este endpoint retorna a especificação OpenAPI 3.0 completa em formato JSON, útil para:
- Integração com ferramentas de teste (Postman, Insomnia)
- Geração automática de clientes
- CI/CD e testes automatizados

## Endpoints Disponíveis

### 1. Evolution Webhook
- **Endpoint**: `POST /api/webhook/evolution`
- **Descrição**: Recebe webhooks da Evolution API com mensagens do WhatsApp
- **Content-Type**: application/json

**Exemplo de Payload:**
```json
{
  "event": "messages.upsert",
  "instance": "chatbot",
  "data": {
    "key": {
      "remoteJid": "5511999999999@s.whatsapp.net",
      "fromMe": false,
      "id": "ABC123"
    },
    "message": {
      "conversation": "novo"
    }
  }
}
```

### 2. GLPI Webhook
- **Endpoint**: `POST /api/webhook/glpi/notification`
- **Descrição**: Recebe notificações de mudanças em tickets do GLPI
- **Content-Type**: application/json

**Exemplo de Payload:**
```json
{
  "ticketId": 123,
  "eventType": "TICKET_ASSIGNED",
  "status": "Em atendimento",
  "assignedTo": "João Silva",
  "phone": "5511999999999",
  "message": "Seu chamado foi atribuído"
}
```

### 3. Health Checks
- **Endpoint**: `GET /api/webhook/evolution/health`
- **Descrição**: Verifica se o serviço Evolution está respondendo

- **Endpoint**: `GET /api/webhook/glpi/health`
- **Descrição**: Verifica se o serviço GLPI está respondendo

## Testando pelo Swagger UI

### Passo 1: Acesse o Swagger UI
Abra seu navegador e acesse: `http://localhost:8082/swagger-ui.html`

### Passo 2: Selecione um Endpoint
Na interface, você verá todos os endpoints agrupados por controller:
- **evolution-webhook-controller**: Endpoints do webhook Evolution
- **glpi-webhook-controller**: Endpoints do webhook GLPI

### Passo 3: Expandir o Endpoint
Clique no endpoint que deseja testar para expandir os detalhes.

### Passo 4: Clique em "Try it out"
No canto superior direito da seção do endpoint, clique no botão "Try it out".

### Passo 5: Preencha os Parâmetros
- Para requisições POST: Edite o JSON de exemplo com seus dados
- Para requisições GET: Não há parâmetros necessários nos health checks

### Passo 6: Execute
Clique no botão "Execute" para enviar a requisição.

### Passo 7: Ver Resposta
Abaixo você verá:
- **Code**: Status HTTP da resposta (200, 400, 500, etc.)
- **Response body**: Corpo da resposta
- **Response headers**: Cabeçalhos HTTP
- **Request duration**: Tempo de execução da requisição

## Exportando a Especificação

### Para Postman
1. Acesse: `http://localhost:8082/v3/api-docs`
2. Copie o JSON completo
3. No Postman: Import → Raw text → Cole o JSON
4. Todos os endpoints serão importados automaticamente

### Para Insomnia
1. Acesse: `http://localhost:8082/v3/api-docs`
2. Copie o JSON completo
3. No Insomnia: Application → Preferences → Data → Import Data
4. Cole o JSON e importe

## Recursos Avançados

### Schemas
No final da página do Swagger UI, você encontra a seção "Schemas" com todos os modelos de dados:
- WebhookEvent
- GlpiWebhookEvent
- Conversation
- Ticket
- etc.

Isso ajuda a entender a estrutura completa dos objetos.

### Tags
Os endpoints são organizados por tags (controllers), facilitando a navegação.

### Respostas HTTP
Cada endpoint mostra todas as possíveis respostas:
- 200: Success
- 400: Bad Request
- 500: Internal Server Error

## Segurança

⚠️ **Importante**: O Swagger UI está disponível apenas em ambiente de desenvolvimento.

Para produção, considere:
- Desabilitar o Swagger: `springdoc.swagger-ui.enabled=false`
- Proteger com autenticação básica
- Expor apenas em rede interna/VPN

## Troubleshooting

### Swagger UI não abre
- Verifique se a aplicação está rodando: `docker ps`
- Verifique os logs: `docker logs chatbot-glpi`
- Confirme a porta correta: `8082`

### Endpoints não aparecem
- Verifique o pacote configurado: `springdoc.packages-to-scan=com.chatbot.chatbotglpi`
- Confirme que os controllers estão anotados com `@RestController`

### Erro ao executar requisição
- Verifique se o payload JSON está correto
- Confirme que o Content-Type é `application/json`
- Veja os logs da aplicação para mais detalhes

## URLs Úteis

| Recurso | URL |
|---------|-----|
| Swagger UI | http://localhost:8082/swagger-ui.html |
| OpenAPI JSON | http://localhost:8082/v3/api-docs |
| OpenAPI YAML | http://localhost:8082/v3/api-docs.yaml |
| Actuator Health | http://localhost:8082/actuator/health |
| Prometheus Metrics | http://localhost:8082/actuator/prometheus |

---

**Dica**: Use o Swagger UI durante o desenvolvimento para testar rapidamente os endpoints sem precisar de curl ou Postman!
