# Chatbot GLPI - WhatsApp

Bot conversacional para abertura de chamados no GLPI via WhatsApp.

---

## 🎯 Objetivo

- Usuário conversa com bot via WhatsApp
- Bot coleta: título, descrição, categoria, urgência
- Dados guardados temporariamente no Redis
- Ao confirmar, bot cria ticket no GLPI
- Redis é limpo (não salva nada permanentemente)

---

## 🏗️ Arquitetura

```
WhatsApp → Evolution API → Chatbot → Redis (temp) → GLPI
```

**Stack:**
- Java 21 + Spring Boot 3.5.6
- Redis (cache temporário 30min)
- Evolution API (WhatsApp)
- GLPI API (tickets)

---

## 🚀 Setup Rápido

### 1. Pré-requisitos
```bash
java -version   # Java 21+
mvn -version    # Maven 3.8+
docker --version
```

### 2. Configurar variáveis
```bash
cp .env.example .env
nano .env  # Editar com suas credenciais
```

### 3. Subir Redis
```bash
docker run -d --name chatbot-redis -p 6379:6379 redis:7-alpine
```

### 4. Executar
```bash
mvn clean install
mvn spring-boot:run
```

### 5. Testar
```bash
curl -X POST "http://localhost:8080/api/test/message?phone=5511999999999&message=oi"
```

---

## ✅ O que está pronto

- [x] Estrutura Spring Boot
- [x] Redis configurado
- [x] ConversationManager (lógica conversacional completa)
- [x] 7 estados de conversa implementados
- [x] Validações de input
- [x] Limpeza automática após criar ticket
- [x] TestController para testes locais

---

## 📋 O que falta implementar

### Fase 1: Integração Evolution API (WhatsApp)
- [ ] `EvolutionClient.java` - Cliente HTTP para enviar mensagens
- [ ] `WebhookController.java` - Receber mensagens do Evolution
- [ ] `WebhookEvent.java` - DTO do webhook
- [ ] Testar com WhatsApp real

### Fase 2: Integração GLPI
- [ ] `GlpiClient.java` - Cliente HTTP para criar tickets
- [ ] `CreateTicketRequest.java` - DTO request
- [ ] `CreateTicketResponse.java` - DTO response
- [ ] Mapeamento categorias/urgências para IDs GLPI
- [ ] Substituir MOCK no ConversationManager
- [ ] Testar criação de tickets reais

### Fase 3: Melhorias
- [ ] Tratamento de erros mais robusto
- [ ] Retry em caso de falha GLPI
- [ ] Logs estruturados
- [ ] Métricas
- [ ] Testes automatizados

---

## 📚 Documentação

- **INTEGRACAO.md** - 🔌 **Guia completo de integração Evolution API + GLPI** (LEIA AQUI!)
- **REQUISITOS-SIMPLIFICADO.md** - Requisitos detalhados e fluxos
- **DOCKER.md** - Como usar Docker (desenvolvimento e produção)

---

## 🧪 Testando

### Teste local (sem WhatsApp)
```bash
# 1. Saudação
curl -X POST "http://localhost:8080/api/test/message?phone=5511999999999&message=oi"

# 2. Título
curl -X POST "http://localhost:8080/api/test/message?phone=5511999999999&message=Impressora quebrada"

# 3. Descrição
curl -X POST "http://localhost:8080/api/test/message?phone=5511999999999&message=Não imprime nenhum documento"

# 4. Categoria (1=Hardware)
curl -X POST "http://localhost:8080/api/test/message?phone=5511999999999&message=1"

# 5. Urgência (3=Alta)
curl -X POST "http://localhost:8080/api/test/message?phone=5511999999999&message=3"

# 6. Confirmar
curl -X POST "http://localhost:8080/api/test/message?phone=5511999999999&message=sim"
```

### Ver dados no Redis
```bash
docker exec -it chatbot-redis redis-cli

# Ver conversas ativas
KEYS conversation:*

# Ver dados
GET conversation:5511999999999

# Ver TTL
TTL conversation:5511999999999
```

---

## 🐳 Docker

### Desenvolvimento (apenas Redis)
```bash
docker run -d --name chatbot-redis -p 6379:6379 redis:7-alpine
mvn spring-boot:run
```

### Produção (tudo)
```bash
docker-compose up -d
```

Veja **DOCKER.md** para mais detalhes.

---

## 📁 Estrutura do Código

```
src/main/java/com/chatbot/chatbotglpi/
├── conversation/
│   ├── StateEnum.java              ✅ Estados da conversa
│   ├── ConversationState.java      ✅ Modelo (Redis)
│   └── ConversationManager.java    ✅ Lógica conversacional
│
├── config/
│   └── RedisConfig.java            ✅ Config Redis
│
├── webhook/
│   ├── TestController.java         ✅ Testes locais
│   ├── WebhookController.java      ⏳ TODO
│   └── dto/
│       └── WebhookEvent.java       ⏳ TODO
│
└── integration/
    ├── evolution/
    │   ├── EvolutionClient.java    ⏳ TODO
    │   └── dto/...                 ⏳ TODO
    └── glpi/
        ├── GlpiClient.java         ⏳ TODO
        └── dto/...                 ⏳ TODO
```

---

## 🔧 Variáveis de Ambiente

```bash
# Evolution API
EVOLUTION_API_URL=https://sua-evolution.com
EVOLUTION_API_KEY=sua_chave
EVOLUTION_API_INSTANCE=sua_instancia

# GLPI API
GLPI_API_URL=https://seu-glpi.com/apirest.php
GLPI_API_APP_TOKEN=seu_token
GLPI_API_USER_TOKEN=seu_token

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=senha
```

---

## 📝 Fluxo da Conversa

```
1. GREETING           → "Oi! Qual o título?"
2. COLLECTING_TITLE   → "Descreva o problema"
3. COLLECTING_DESCRIPTION → "Qual a categoria?"
4. COLLECTING_CATEGORY → "Qual a urgência?"
5. COLLECTING_URGENCY  → "Confirma os dados?"
6. CONFIRMING         → Cria ticket no GLPI
7. COMPLETED          → "Ticket #12345 criado!"
```

---

## 🆘 Troubleshooting

**Redis não conecta:**
```bash
docker ps | grep redis
docker logs chatbot-redis
```

**Porta 8080 em uso:**
```bash
# Mudar em application.properties
server.port=8081
```

---

**Versão:** 1.0.0
**Última atualização:** 14/11/2025
