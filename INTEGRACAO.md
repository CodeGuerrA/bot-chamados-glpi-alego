# 🔌 Guia de Integração - Evolution API + GLPI

Este guia ensina como integrar seu chatbot com **Evolution API (WhatsApp)** e **GLPI** para criar um sistema completo e funcional.

---

## 📋 Índice

1. [Integração Evolution API (WhatsApp)](#1-integração-evolution-api-whatsapp)
2. [Integração GLPI](#2-integração-glpi)
3. [Testando as Integrações](#3-testando-as-integrações)
4. [Troubleshooting](#4-troubleshooting)

---

## 1. Integração Evolution API (WhatsApp)

A Evolution API é responsável por conectar seu chatbot ao WhatsApp. Você precisa:
- **Receber** mensagens dos usuários via Webhook
- **Enviar** respostas de volta para o WhatsApp

### 1.1. Pré-requisitos

1. **Instalar Evolution API**
   - URL oficial: https://github.com/EvolutionAPI/evolution-api
   - Deploy recomendado: Docker ou servidor dedicado
   - Obter: API_KEY, INSTANCE_NAME

2. **Configurar Webhook**
   - No painel da Evolution API, configure o webhook para: `https://seu-servidor.com/api/webhook/evolution`

### 1.2. Estrutura de Pastas

Crie a seguinte estrutura:

```
src/main/java/com/chatbot/chatbotglpi/
├── integration/
│   └── evolution/
│       ├── EvolutionClient.java          # Cliente HTTP
│       ├── EvolutionService.java         # Lógica de envio
│       └── dto/
│           ├── WebhookEvent.java         # Webhook recebido
│           ├── SendMessageRequest.java   # Request para enviar msg
│           └── SendMessageResponse.java  # Response do envio
│
└── webhook/
    └── WebhookController.java            # Controller que recebe webhook
```

---

### 1.3. Passo 1: Criar DTOs

#### **WebhookEvent.java**
```java
package com.chatbot.chatbotglpi.integration.evolution.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookEvent {

    private String event;         // Tipo de evento (ex: "messages.upsert")
    private String instance;      // Nome da instância
    private Data data;           // Dados da mensagem

    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {
        private Key key;
        private Message message;

        @lombok.Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Key {
            private String remoteJid;     // Número do remetente (5511999999999@s.whatsapp.net)
            private boolean fromMe;       // true se foi enviado pelo bot
            private String id;            // ID da mensagem
        }

        @lombok.Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Message {
            private String conversation;  // Texto da mensagem

            @JsonProperty("extendedTextMessage")
            private ExtendedText extendedTextMessage;

            @lombok.Data
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class ExtendedText {
                private String text;      // Texto quando é resposta/citação
            }
        }
    }

    // Método auxiliar para extrair o número de telefone
    public String getPhoneNumber() {
        if (data == null || data.key == null) return null;

        String remoteJid = data.key.remoteJid;
        // Remove @s.whatsapp.net e retorna apenas o número
        return remoteJid.replace("@s.whatsapp.net", "");
    }

    // Método auxiliar para extrair o texto da mensagem
    public String getMessageText() {
        if (data == null || data.message == null) return null;

        // Tenta pegar o texto direto
        if (data.message.conversation != null) {
            return data.message.conversation;
        }

        // Tenta pegar de extendedTextMessage (quando é resposta)
        if (data.message.extendedTextMessage != null) {
            return data.message.extendedTextMessage.text;
        }

        return null;
    }

    // Verifica se a mensagem foi enviada pelo bot (ignora)
    public boolean isFromMe() {
        return data != null && data.key != null && data.key.fromMe;
    }
}
```

#### **SendMessageRequest.java**
```java
package com.chatbot.chatbotglpi.integration.evolution.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SendMessageRequest {

    @JsonProperty("number")
    private String number;        // Número sem @s.whatsapp.net

    @JsonProperty("text")
    private String text;          // Texto da mensagem

    @JsonProperty("delay")
    private Integer delay;        // Delay em ms (opcional, default: 1000)
}
```

#### **SendMessageResponse.java**
```java
package com.chatbot.chatbotglpi.integration.evolution.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SendMessageResponse {
    private String key;
    private String status;
    private String message;
}
```

---

### 1.4. Passo 2: Criar Cliente HTTP

#### **EvolutionClient.java**
```java
package com.chatbot.chatbotglpi.integration.evolution;

import com.chatbot.chatbotglpi.integration.evolution.dto.SendMessageRequest;
import com.chatbot.chatbotglpi.integration.evolution.dto.SendMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class EvolutionClient {

    private final RestTemplate restTemplate;

    @Value("${evolution.api.url}")
    private String evolutionApiUrl;

    @Value("${evolution.api.key}")
    private String evolutionApiKey;

    @Value("${evolution.api.instance}")
    private String evolutionInstance;

    /**
     * Envia mensagem de texto via Evolution API
     */
    public SendMessageResponse sendTextMessage(String phoneNumber, String message) {
        try {
            // Remove caracteres especiais do número (apenas dígitos)
            String cleanPhone = phoneNumber.replaceAll("[^0-9]", "");

            // Monta a URL: https://api.com/message/sendText/INSTANCE_NAME
            String url = String.format("%s/message/sendText/%s",
                evolutionApiUrl,
                evolutionInstance
            );

            // Cria request
            SendMessageRequest request = SendMessageRequest.builder()
                    .number(cleanPhone)
                    .text(message)
                    .delay(1200) // Delay de 1.2s (mais natural)
                    .build();

            // Cria headers com autenticação
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("apikey", evolutionApiKey);

            // Cria entidade HTTP
            HttpEntity<SendMessageRequest> entity = new HttpEntity<>(request, headers);

            // Faz chamada POST
            ResponseEntity<SendMessageResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    SendMessageResponse.class
            );

            log.info("Mensagem enviada para {}: {}", cleanPhone, response.getStatusCode());
            return response.getBody();

        } catch (Exception e) {
            log.error("Erro ao enviar mensagem via Evolution API para {}: ", phoneNumber, e);
            throw new RuntimeException("Falha ao enviar mensagem WhatsApp", e);
        }
    }
}
```

---

### 1.5. Passo 3: Criar Service (Opcional, mas recomendado)

#### **EvolutionService.java**

```java
package com.chatbot.chatbotglpi.integration.evolution;

import com.chatbot.chatbotglpi.integration.evolution.dto.SendMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvolutionService {

    private final EvolutionClient evolutionClient;

    /**
     * Envia mensagem com retry automático em caso de falha
     */
    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void sendMessage(String phoneNumber, String message) {
        try {
            SendMessageResponse response = evolutionClient.sendTextMessage(phoneNumber, message);
            log.debug("Mensagem enviada com sucesso: {}", response);
        } catch (Exception e) {
            log.error("Falha ao enviar mensagem após retries", e);
            // Aqui você pode decidir se lança exceção ou registra em fila de retry
            throw e;
        }
    }
}
```

---

### 1.6. Passo 4: Criar Webhook Controller

#### **WebhookController.java**

```java
package com.chatbot.chatbotglpi.webhook;

import com.chatbot.chatbotglpi.integration.evolution.EvolutionService;
import com.chatbot.chatbotglpi.integration.evolution.dto.WebhookEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final ConversationManager conversationManager;
    private final EvolutionService evolutionService;

    /**
     * Endpoint que recebe webhooks da Evolution API
     */
    @PostMapping("/evolution")
    public ResponseEntity<Void> handleEvolutionWebhook(@RequestBody WebhookEvent event) {
        try {
            log.info("Webhook recebido: {}", event.getEvent());

            // Filtra apenas eventos de mensagens recebidas
            if (!"messages.upsert".equals(event.getEvent())) {
                log.debug("Evento ignorado: {}", event.getEvent());
                return ResponseEntity.ok().build();
            }

            // Ignora mensagens enviadas pelo próprio bot
            if (event.isFromMe()) {
                log.debug("Mensagem enviada pelo bot, ignorando");
                return ResponseEntity.ok().build();
            }

            // Extrai dados da mensagem
            String phone = event.getPhoneNumber();
            String message = event.getMessageText();

            // Valida dados
            if (phone == null || message == null || message.trim().isEmpty()) {
                log.warn("Webhook com dados inválidos: phone={}, message={}", phone, message);
                return ResponseEntity.ok().build();
            }

            log.info("Processando mensagem de {}: {}", phone, message);

            // Processa mensagem através do ConversationManager
            String response = conversationManager.processMessage(phone, message);

            // Envia resposta de volta via WhatsApp
            evolutionService.sendMessage(phone, response);

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Erro ao processar webhook Evolution", e);
            // Retorna 200 para evitar reenvios do webhook
            return ResponseEntity.ok().build();
        }
    }

    /**
     * Endpoint de health check para Evolution API verificar
     */
    @GetMapping("/evolution/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
```

---

### 1.7. Passo 5: Configurar application.properties

```properties
# Evolution API
evolution.api.url=https://sua-evolution-api.com
evolution.api.key=SEU_API_KEY_AQUI
evolution.api.instance=NOME_DA_SUA_INSTANCIA
```

---

### 1.8. Passo 6: Adicionar Dependência (se necessário)

No `pom.xml`, certifique-se de ter:

```xml
<!-- Spring Retry para retry automático -->
<dependency>
    <groupId>org.springframework.retry</groupId>
    <artifactId>spring-retry</artifactId>
</dependency>

<!-- Para habilitar @Retryable -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

E habilite retry na classe principal:

```java
@SpringBootApplication
@EnableRetry  // Adicione esta anotação
public class ChatbotGlpiApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatbotGlpiApplication.class, args);
    }
}
```

---

### 1.9. Remover TestController (Produção)

Após integrar com Evolution API, você pode:
- **Opção 1:** Deletar `TestController.java`
- **Opção 2:** Deixar apenas em profile `dev`:

```java
@RestController
@RequestMapping("/api/test")
@Profile("dev")  // Só ativa em desenvolvimento
public class TestController {
    // ...
}
```

---

## 2. Integração GLPI

O GLPI é o sistema de tickets. Você precisa criar tickets reais substituindo o MOCK.

### 2.1. Pré-requisitos

1. **Acesso GLPI**
   - URL da API: `https://seu-glpi.com/apirest.php`
   - App Token: Criar em Setup > Geral > API
   - User Token: Criar no perfil do usuário

2. **Mapeamento de IDs**
   - Descobrir IDs de categorias no GLPI
   - Descobrir IDs de prioridades/urgências
   - Descobrir ID da entidade padrão

---

### 2.2. Estrutura de Pastas

```
src/main/java/com/chatbot/chatbotglpi/
└── integration/
    └── glpi/
        ├── GlpiClient.java               # Cliente HTTP
        ├── GlpiService.java              # Lógica de negócio
        └── dto/
            ├── CreateTicketRequest.java   # Request
            ├── CreateTicketResponse.java  # Response
            └── GlpiSessionResponse.java   # Resposta do init_session
```

---

### 2.3. Passo 1: Criar DTOs

#### **CreateTicketRequest.java**
```java
package com.chatbot.chatbotglpi.integration.glpi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateTicketRequest {

    @JsonProperty("name")
    private String name;                    // Título do ticket

    @JsonProperty("content")
    private String content;                 // Descrição/conteúdo

    @JsonProperty("itilcategories_id")
    private Integer itilcategoriesId;       // ID da categoria GLPI

    @JsonProperty("urgency")
    private Integer urgency;                // 1=Baixa, 2=Média, 3=Alta, 4=Muito Alta, 5=Crítica

    @JsonProperty("entities_id")
    private Integer entitiesId;             // ID da entidade (geralmente 0 ou 1)

    @JsonProperty("type")
    private Integer type;                   // 1=Incidente, 2=Requisição

    @JsonProperty("status")
    private Integer status;                 // 1=Novo, 2=Em andamento, etc
}
```

#### **CreateTicketResponse.java**
```java
package com.chatbot.chatbotglpi.integration.glpi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateTicketResponse {
    private Integer id;           // ID do ticket criado
    private String message;       // Mensagem de sucesso/erro
}
```

#### **GlpiSessionResponse.java**
```java
package com.chatbot.chatbotglpi.integration.glpi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GlpiSessionResponse {

    @JsonProperty("session_token")
    private String sessionToken;
}
```

---

### 2.4. Passo 2: Criar Cliente GLPI

#### **GlpiClient.java**
```java
package com.chatbot.chatbotglpi.integration.glpi;

import com.chatbot.chatbotglpi.integration.glpi.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class GlpiClient {

    private final RestTemplate restTemplate;

    @Value("${glpi.api.url}")
    private String glpiApiUrl;

    @Value("${glpi.api.app-token}")
    private String appToken;

    @Value("${glpi.api.user-token}")
    private String userToken;

    public GlpiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Inicia sessão no GLPI e retorna o session token
     */
    private String initSession() {
        try {
            String url = glpiApiUrl + "/initSession";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("App-Token", appToken);
            headers.set("Authorization", "user_token " + userToken);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<GlpiSessionResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    GlpiSessionResponse.class
            );

            String sessionToken = response.getBody().getSessionToken();
            log.debug("Sessão GLPI iniciada: {}", sessionToken);
            return sessionToken;

        } catch (Exception e) {
            log.error("Erro ao iniciar sessão GLPI", e);
            throw new RuntimeException("Falha ao autenticar no GLPI", e);
        }
    }

    /**
     * Encerra sessão no GLPI
     */
    private void killSession(String sessionToken) {
        try {
            String url = glpiApiUrl + "/killSession";

            HttpHeaders headers = new HttpHeaders();
            headers.set("App-Token", appToken);
            headers.set("Session-Token", sessionToken);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            log.debug("Sessão GLPI encerrada");

        } catch (Exception e) {
            log.warn("Erro ao encerrar sessão GLPI (não crítico)", e);
        }
    }

    /**
     * Cria um ticket no GLPI
     */
    public CreateTicketResponse createTicket(CreateTicketRequest request) {
        String sessionToken = null;

        try {
            // 1. Inicia sessão
            sessionToken = initSession();

            // 2. Cria ticket
            String url = glpiApiUrl + "/Ticket";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("App-Token", appToken);
            headers.set("Session-Token", sessionToken);

            HttpEntity<CreateTicketRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<CreateTicketResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    CreateTicketResponse.class
            );

            log.info("Ticket GLPI criado: #{}", response.getBody().getId());
            return response.getBody();

        } catch (Exception e) {
            log.error("Erro ao criar ticket no GLPI", e);
            throw new RuntimeException("Falha ao criar ticket no GLPI", e);

        } finally {
            // 3. Sempre encerra sessão
            if (sessionToken != null) {
                killSession(sessionToken);
            }
        }
    }
}
```

---

### 2.5. Passo 3: Criar Service com Mapeamentos

#### **GlpiService.java**

```java
package com.chatbot.chatbotglpi.integration.glpi;

import com.chatbot.chatbotglpi.integration.glpi.dto.CreateTicketRequest;
import com.chatbot.chatbotglpi.integration.glpi.dto.CreateTicketResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GlpiService {

    private final GlpiClient glpiClient;

    @Value("${glpi.default.entity-id:0}")
    private Integer defaultEntityId;

    /**
     * Cria ticket no GLPI a partir dos dados da conversa
     */
    public CreateTicketResponse createTicketFromConversation(com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState state) {

        // Monta request
        CreateTicketRequest request = CreateTicketRequest.builder()
                .name(state.getData("title"))
                .content(state.getData("description"))
                .itilcategoriesId(mapCategoryToGlpiId(state.getData("category")))
                .urgency(mapUrgencyToGlpiId(state.getData("urgency")))
                .entitiesId(defaultEntityId)
                .type(1)      // 1 = Incidente
                .status(1)    // 1 = Novo
                .build();

        // Cria ticket
        return glpiClient.createTicket(request);
    }

    /**
     * Mapeia categoria do chatbot para ID da categoria no GLPI
     *
     * ATENÇÃO: Você precisa descobrir os IDs reais no seu GLPI!
     * Acesse: Setup > Dropdowns > Categoria de Ticket
     */
    private Integer mapCategoryToGlpiId(String category) {
        return switch (category) {
            case "Hardware" -> 1;      // ⚠️ ALTERE PARA O ID REAL
            case "Software" -> 2;      // ⚠️ ALTERE PARA O ID REAL
            case "Rede" -> 3;          // ⚠️ ALTERE PARA O ID REAL
            case "Acesso" -> 4;        // ⚠️ ALTERE PARA O ID REAL
            default -> 0;              // Sem categoria
        };
    }

    /**
     * Mapeia urgência do chatbot para valor GLPI
     *
     * GLPI usa:
     * 1 = Muito baixa
     * 2 = Baixa
     * 3 = Média
     * 4 = Alta
     * 5 = Muito alta
     */
    private Integer mapUrgencyToGlpiId(String urgency) {
        return switch (urgency) {
            case "Baixa" -> 2;
            case "Média" -> 3;
            case "Alta" -> 4;
            case "Crítica" -> 5;
            default -> 3;  // Padrão: Média
        };
    }
}
```

---

### 2.6. Passo 4: Substituir MOCK no ConversationManager

Agora vamos substituir o ticket fictício pelo real:

```java
package com.chatbot.chatbotglpi.conversation;

import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import com.chatbot.chatbotglpi.conversation.domain.enums.StateEnum;
import com.chatbot.chatbotglpi.integration.glpi.GlpiService;
import com.chatbot.chatbotglpi.integration.glpi.dto.CreateTicketResponse;
// ... outros imports

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationManager {

    // Injeta o GlpiService
    private final GlpiService glpiService;

    // ... outros campos

    // --------------------------------------------------------
    // FUNÇÃO: cria ticket REAL no GLPI e finaliza conversa
    private String createTicketAndFinish(ConversationState state) {
        try {
            // Verifica se todos dados estão preenchidos
            if (!state.isComplete()) {
                return "❌ Não foi possível criar o chamado. Dados incompletos. Digite *oi* para começar novamente.";
            }

            // ✅ CRIA TICKET REAL NO GLPI (substituindo o mock)
            CreateTicketResponse ticketResponse = glpiService.createTicketFromConversation(state);
            Integer ticketId = ticketResponse.getId();

            // Marca conversa como completa
            state.setCurrentState(StateEnum.COMPLETED);

            // Remove conversa do Redis E invalida cache L2
            deleteConversationState(state.getPhone());

            // Loga criação do ticket
            log.info("Ticket criado no GLPI: #{} para usuário {}", ticketId, state.getPhone());

            // Retorna mensagem final para usuário
            return String.format("""
                            ✅ Chamado criado com sucesso!
                            📋 Número: #%d
                            📌 Título: %s
                            ✍️ Descrição: %s
                            📁 Categoria: %s
                            🔔 Urgência: %s
                            ⏰ Status: Em análise
                            
                            Você receberá atualizações sobre seu chamado.
                            
                            Digite *oi* para abrir um novo chamado.
                            """,
                    ticketId,
                    state.getData("title"),
                    state.getData("description"),
                    state.getData("category"),
                    state.getData("urgency")
            );

        } catch (Exception e) {
            log.error("Erro ao criar ticket no GLPI", e);
            return "❌ Erro ao criar o chamado. Tente novamente em alguns instantes ou entre em contato com o suporte.";
        }
    }

    // ... resto do código
}
```

---

### 2.7. Passo 5: Configurar application.properties

```properties
# GLPI API
glpi.api.url=https://seu-glpi.com/apirest.php
glpi.api.app-token=SEU_APP_TOKEN_AQUI
glpi.api.user-token=SEU_USER_TOKEN_AQUI
glpi.default.entity-id=0
```

---

### 2.8. Como Descobrir IDs das Categorias no GLPI

1. **Via Interface Web:**
   - Acesse: `Configurar > Dropdowns > Categorias de chamados`
   - Ao editar uma categoria, veja o ID na URL: `?id=123`

2. **Via API (Recomendado):**

```bash
# 1. Inicia sessão
curl -X GET "https://seu-glpi.com/apirest.php/initSession" \
  -H "App-Token: SEU_APP_TOKEN" \
  -H "Authorization: user_token SEU_USER_TOKEN"

# Retorna: {"session_token": "abc123xyz"}

# 2. Lista categorias
curl -X GET "https://seu-glpi.com/apirest.php/ITILCategory" \
  -H "App-Token: SEU_APP_TOKEN" \
  -H "Session-Token: abc123xyz"

# Retorna lista com: id, name, etc.

# 3. Encerra sessão
curl -X GET "https://seu-glpi.com/apirest.php/killSession" \
  -H "App-Token: SEU_APP_TOKEN" \
  -H "Session-Token: abc123xyz"
```

3. **Criar script auxiliar (Opcional):**

```java
// Crie um endpoint temporário em WebhookController

@GetMapping("/debug/glpi-categories")
public ResponseEntity<?> debugCategories() {
    // Chama API do GLPI para listar categorias
    // Retorna JSON com IDs e nomes
    return ResponseEntity.ok(glpiClient.listCategories());
}
```

---

## 3. Testando as Integrações

### 3.1. Teste Evolution API

1. **Configure o webhook** na Evolution API apontando para seu servidor
2. **Envie mensagem** via WhatsApp para o número conectado
3. **Verifique logs:**

```bash
# Logs do Spring Boot
tail -f logs/application.log | grep "Webhook recebido"

# Deve aparecer:
# Webhook recebido: messages.upsert
# Processando mensagem de 5511999999999: oi
```

4. **Deve receber resposta** no WhatsApp com a saudação do bot

---

### 3.2. Teste GLPI

1. **Configure tokens** no application.properties
2. **Teste manualmente** criação de ticket:

```java
// Crie um endpoint de teste temporário

@GetMapping("/debug/create-ticket")
public ResponseEntity<?> debugCreateTicket() {
    CreateTicketRequest request = CreateTicketRequest.builder()
            .name("Teste via API")
            .content("Testando integração chatbot")
            .itilcategoriesId(1)
            .urgency(3)
            .entitiesId(0)
            .type(1)
            .status(1)
            .build();

    CreateTicketResponse response = glpiClient.createTicket(request);
    return ResponseEntity.ok(response);
}
```

3. **Acesse:** `http://localhost:8080/debug/create-ticket`
4. **Verifique** no GLPI se o ticket foi criado

---

### 3.3. Teste Integração Completa

1. **Envie "oi"** via WhatsApp
2. **Preencha todos os campos** (descrição, categoria, urgência)
3. **Confirme** com "sim"
4. **Verifique:**
   - Bot responde com número do ticket
   - Ticket aparece no GLPI
   - Redis foi limpo: `redis-cli KEYS conversation:*` (deve estar vazio)

---

## 4. Troubleshooting

### 4.1. Evolution API não recebe mensagens

**Sintomas:**
- Mensagens enviadas no WhatsApp não chegam no webhook

**Soluções:**
```bash
# 1. Verifique se o webhook está configurado
# No painel Evolution, veja: Settings > Webhook

# 2. Teste se o endpoint está acessível
curl -X POST https://seu-servidor.com/api/webhook/evolution \
  -H "Content-Type: application/json" \
  -d '{"event":"test"}'

# 3. Verifique se a instância está conectada
# No painel Evolution: Status deve estar "open"

# 4. Veja logs da Evolution API
docker logs evolution-api
```

---

### 4.2. GLPI retorna erro 401 (Não autorizado)

**Sintomas:**
- Erro ao criar ticket: `401 Unauthorized`

**Soluções:**
```bash
# 1. Verifique se os tokens estão corretos
echo $GLPI_API_APP_TOKEN
echo $GLPI_API_USER_TOKEN

# 2. Teste manualmente via curl
curl -X GET "https://seu-glpi.com/apirest.php/initSession" \
  -H "App-Token: SEU_APP_TOKEN" \
  -H "Authorization: user_token SEU_USER_TOKEN"

# Se retornar erro, recrie os tokens no GLPI

# 3. Verifique se a API está habilitada no GLPI
# Setup > Geral > API > Habilitar API REST
```

---

### 4.3. GLPI retorna erro 400 (Bad Request)

**Sintomas:**
- Ticket não é criado, erro 400

**Possíveis causas:**
- ID de categoria inválido
- ID de entidade inválido
- Campos obrigatórios faltando

**Solução:**
```bash
# Liste as categorias válidas
curl -X GET "https://seu-glpi.com/apirest.php/ITILCategory" \
  -H "App-Token: SEU_APP_TOKEN" \
  -H "Session-Token: SEU_SESSION_TOKEN"

# Ajuste os IDs em GlpiService.java
```

---

### 4.4. Mensagens duplicadas no WhatsApp

**Sintomas:**
- Bot responde 2x ou mais para a mesma mensagem

**Causa:**
- Evolution API reenvia webhook em caso de timeout

**Solução:**
```java
// Adicione idempotência no WebhookController

private final Set<String> processedMessageIds = new ConcurrentHashSet<>();

@PostMapping("/evolution")
public ResponseEntity<Void> handleEvolutionWebhook(@RequestBody WebhookEvent event) {
    String messageId = event.getData().getKey().getId();

    // Verifica se já processou esta mensagem
    if (processedMessageIds.contains(messageId)) {
        log.debug("Mensagem já processada: {}", messageId);
        return ResponseEntity.ok().build();
    }

    // Marca como processada
    processedMessageIds.add(messageId);

    // Limpa IDs antigos (mantenha apenas últimos 1000)
    if (processedMessageIds.size() > 1000) {
        processedMessageIds.clear();
    }

    // ... resto do código
}
```

---

### 4.5. Timeout ao criar ticket GLPI

**Sintomas:**
- Demora muito para criar ticket ou dá timeout

**Solução:**
```java
// Configure timeout no RestTemplate

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory factory =
            new HttpComponentsClientHttpRequestFactory();

        factory.setConnectTimeout(5000);  // 5s para conectar
        factory.setReadTimeout(10000);    // 10s para ler resposta

        return new RestTemplate(factory);
    }
}
```

---

## 5. Checklist Final

Antes de ir para produção, verifique:

### Evolution API
- [ ] Webhook configurado e apontando para seu servidor
- [ ] Servidor acessível via HTTPS (Evolution não aceita HTTP)
- [ ] API Key e Instance Name corretos
- [ ] Mensagens sendo recebidas e respondidas
- [ ] Logs sem erros

### GLPI
- [ ] App Token e User Token válidos
- [ ] IDs de categorias mapeados corretamente
- [ ] Tickets sendo criados com sucesso
- [ ] Tickets aparecem no GLPI com dados corretos
- [ ] Sessões sendo encerradas corretamente

### Geral
- [ ] TestController desabilitado em produção (`@Profile("dev")`)
- [ ] Logs estruturados e sem informações sensíveis
- [ ] Retry configurado para chamadas externas
- [ ] Error handling robusto
- [ ] Timeout configurado no RestTemplate

---

## 6. Próximos Passos

Após integrar Evolution API + GLPI:

1. **Monitoramento**
   - Adicionar métricas (Prometheus)
   - Configurar alertas (Grafana)
   - Logs centralizados (ELK Stack)

2. **Segurança**
   - Rate limiting
   - Validação de webhooks (assinatura)
   - Secrets em vault (não em .env)

3. **Melhorias**
   - Suporte a imagens/anexos
   - Atualizar usuário sobre status do ticket
   - Consultar tickets abertos
   - Chatbot com IA (GPT) para gerar títulos melhores

---

**Documentação criada em:** 15/11/2025
**Versão:** 1.0.0
