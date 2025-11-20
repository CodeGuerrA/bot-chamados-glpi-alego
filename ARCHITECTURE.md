# 🏗️ Arquitetura do Projeto - Mapa Completo

> **Guia de Referência Rápida**: O que cada pacote e classe faz no sistema

---

## 📑 Índice

1. [Visão Geral da Arquitetura](#visão-geral-da-arquitetura)
2. [Camada de Domínio](#-camada-de-domínio-conversation)
3. [Camada de Aplicação](#-camada-de-aplicação-conversation)
4. [Camada de Infraestrutura](#-camada-de-infraestrutura-conversation)
5. [Integrações Externas](#-integrações-externas-integration)
6. [Shared (Código Compartilhado)](#-shared-código-compartilhado)
7. [Fluxo de Dados](#-fluxo-de-dados)
8. [Diagrama de Dependências](#-diagrama-de-dependências)

---

## Visão Geral da Arquitetura

O projeto segue **Arquitetura Hexagonal (Ports & Adapters)** com **Domain-Driven Design (DDD)**:

```
┌─────────────────────────────────────────────────────────────┐
│                    ENTRYPOINTS                              │
│         (Controllers, Webhooks, Schedulers)                 │
└───────────────────────┬─────────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────────┐
│              APPLICATION LAYER                              │
│     (Use Cases, Facades, Services, Ports)                   │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐  │
│  │           DOMAIN LAYER                               │  │
│  │  (Entities, States, Validators, Domain Services)     │  │
│  └──────────────────────────────────────────────────────┘  │
└───────────────────────┬─────────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────────┐
│            INFRASTRUCTURE LAYER                             │
│  (Redis, HTTP Clients, Cache, Metrics, Schedulers)         │
└─────────────────────────────────────────────────────────────┘
```

### Princípios Seguidos

- ✅ **Dependency Inversion**: Domínio não depende de infraestrutura
- ✅ **Single Responsibility**: Cada classe tem uma única responsabilidade
- ✅ **Open/Closed**: Extensível sem modificar código existente
- ✅ **Interface Segregation**: Interfaces pequenas e específicas
- ✅ **Separation of Concerns**: Camadas bem separadas

---

## 🎯 Camada de Domínio (conversation)

### `conversation/domain/`

**Responsabilidade**: Contém as **regras de negócio** e lógica central do chatbot. Não depende de nada externo.

---

### 📦 `domain/entity/`

Entidades que representam conceitos de negócio.

#### `ConversationState.java`
```java
// O QUE FAZ: Representa o estado completo de uma conversa com um usuário
// CAMPOS PRINCIPAIS:
// - phone: Telefone do usuário (chave única)
// - currentState: Estado atual da máquina de estados
// - ticketType: Tipo do chamado (Hardware, Software, etc)
// - category: Categoria específica (Impressora, Monitor, etc)
// - description: Descrição do problema
// - location: Localização física
// - ramal: Ramal telefônico
// - username: Nome do usuário
// - urgency: Urgência do chamado
// - lastUpdate: Última atualização
// - title: Título gerado automaticamente

// QUANDO USAR: Sempre que precisar armazenar/recuperar dados de conversa
```

#### `TicketFeedback.java`
```java
// O QUE FAZ: Armazena feedback do usuário sobre atendimento
// CAMPOS:
// - ticketId: ID do chamado
// - rating: Avaliação (1-5)
// - comment: Comentário opcional
// - phone: Telefone do usuário

// QUANDO USAR: Para coletar satisfação do cliente
```

---

### 📦 `domain/enums/`

Enumerações que definem valores fixos.

#### `StateEnum.java`
```java
// O QUE FAZ: Define todos os estados possíveis da conversa
// ESTADOS:
// - GREETING: Saudação inicial
// - COLLECTING_CATEGORY: Coletando tipo/categoria
// - COLLECTING_DESCRIPTION: Coletando descrição
// - COLLECTING_LOCATION: Coletando localização
// - COLLECTING_RAMAL: Coletando ramal
// - COLLECTING_USERNAME: Coletando nome
// - COLLECTING_URGENCY: Coletando urgência
// - CONFIRMING: Confirmação final
// - COMPLETED: Chamado criado

// QUANDO USAR: Para verificar em qual etapa o usuário está
```

---

### 📦 `domain/state/`

**Pattern**: State Machine - Cada classe representa um estado da conversa.

#### `ChatState.java` (Interface)
```java
// O QUE FAZ: Interface base para todos os estados
// MÉTODO PRINCIPAL:
// - processMessage(input, conversation): Processa mensagem do usuário
// - getNextState(): Retorna próximo estado

// QUANDO USAR: Implementar em cada estado concreto
```

#### `GreetingState.java`
```java
// O QUE FAZ: Estado inicial - saúda usuário
// COMPORTAMENTO:
// - Exibe mensagem de boas-vindas
// - Mostra opções de tipo de chamado (Hardware, Software, etc)
// - Aguarda escolha do usuário

// PRÓXIMO ESTADO: CollectingCategoryState
```

#### `CollectingCategoryState.java`
```java
// O QUE FAZ: Coleta categoria específica baseada no tipo
// COMPORTAMENTO:
// - Exibe subcategorias (ex: se Hardware → Impressora, Monitor, etc)
// - Valida escolha
// - Salva categoria na conversa

// PRÓXIMO ESTADO: CollectingDescriptionState
```

#### `CollectingDescriptionState.java`
```java
// O QUE FAZ: Coleta descrição detalhada do problema
// COMPORTAMENTO:
// - Solicita descrição completa
// - Valida tamanho (20-1000 caracteres)
// - Usa DescriptionValidator

// PRÓXIMO ESTADO: CollectingLocationState
```

#### `CollectingLocationState.java`
```java
// O QUE FAZ: Coleta localização física (sala, andar, setor)
// COMPORTAMENTO:
// - Solicita local
// - Valida entrada
// - Salva location

// PRÓXIMO ESTADO: CollectingRamalState
```

#### `CollectingRamalState.java`
```java
// O QUE FAZ: Coleta ramal telefônico
// COMPORTAMENTO:
// - Solicita ramal
// - Valida formato numérico
// - Salva ramal

// PRÓXIMO ESTADO: CollectionUsernameState
```

#### `CollectionUsernameState.java`
```java
// O QUE FAZ: Coleta nome do usuário
// COMPORTAMENTO:
// - Solicita nome completo
// - Valida formato
// - Salva username

// PRÓXIMO ESTADO: CollectingUrgencyState
```

#### `CollectingUrgencyState.java`
```java
// O QUE FAZ: Coleta urgência do chamado
// COMPORTAMENTO:
// - Exibe opções (Baixa, Média, Alta, Muito Alta)
// - Valida escolha
// - Salva urgency

// PRÓXIMO ESTADO: ConfirmingState
```

#### `ConfirmingState.java`
```java
// O QUE FAZ: Exibe resumo e pede confirmação
// COMPORTAMENTO:
// - Mostra todos os dados coletados
// - Aguarda confirmação (sim/não)
// - Se sim → cria ticket no GLPI
// - Se não → volta ao estado anterior

// PRÓXIMO ESTADO: CompletedState (se confirmado)
```

#### `CompletedState.java`
```java
// O QUE FAZ: Estado final - chamado criado
// COMPORTAMENTO:
// - Exibe número do chamado
// - Agradece
// - Limpa conversa do Redis

// PRÓXIMO ESTADO: Nenhum (fim)
```

#### `StateFactory.java`
```java
// O QUE FAZ: Factory para criar instâncias de estados
// MÉTODO:
// - createState(StateEnum): Retorna instância do estado correspondente

// QUANDO USAR: Sempre que precisar obter um estado
// BENEFÍCIO: Centraliza criação, facilita testes
```

---

### 📦 `domain/validator/`

Validadores de entrada do usuário.

#### `Validator.java` (Interface base)
```java
// O QUE FAZ: Interface para todos os validadores
// MÉTODO:
// - validate(input): ValidatedMessage

// QUANDO USAR: Implementar em cada validador específico
```

#### `DescriptionValidator.java`
```java
// O QUE FAZ: Valida descrição do problema
// REGRAS:
// - Mínimo 20 caracteres
// - Máximo 1000 caracteres
// - Não pode ser vazia
// - Remove espaços extras

// RETORNA: ValidatedMessage (válida ou inválida + mensagem erro)
```

#### `LocateValidator.java`
```java
// O QUE FAZ: Valida localização
// REGRAS:
// - Não vazia
// - Sanitiza HTML
// - Remove caracteres especiais

// QUANDO USAR: Ao processar CollectingLocationState
```

#### `RamalValidator.java`
```java
// O QUE FAZ: Valida ramal telefônico
// REGRAS:
// - Deve ser numérico
// - Tamanho adequado (3-5 dígitos)

// QUANDO USAR: Ao processar CollectingRamalState
```

#### `UsernameValidator.java`
```java
// O QUE FAZ: Valida nome do usuário
// REGRAS:
// - Mínimo 3 caracteres
// - Apenas letras e espaços
// - Capitaliza nome

// QUANDO USAR: Ao processar CollectionUsernameState
```

---

### 📦 `domain/service/`

**Domain Services**: Lógica de negócio que não pertence a uma entidade específica.

#### `CategoryMapperService.java`
```java
// O QUE FAZ: Mapeia escolhas do usuário para IDs do GLPI
// EXEMPLO:
// - "1" (Hardware) → 14 (ID categoria GLPI)
// - "1.1" (Impressora) → 45 (ID subcategoria)

// QUANDO USAR: Ao criar ticket no GLPI
// BENEFÍCIO: Centraliza mapeamento categoria ↔ GLPI
```

#### `UrgencyMapperService.java`
```java
// O QUE FAZ: Mapeia urgência para IDs GLPI
// MAPEAMENTO:
// - "Baixa" → 1
// - "Média" → 2
// - "Alta" → 3
// - "Muito Alta" → 4

// QUANDO USAR: Ao criar ticket no GLPI
```

#### `NlpTitleGeneratorService.java`
```java
// O QUE FAZ: Gera título automático do chamado usando NLP
// TÉCNICAS:
// - Extração de palavras-chave
// - Part-of-Speech tagging
// - Análise de verbos/substantivos
// - Limite de 100 caracteres

// ENTRADA: "A impressora HP do 2º andar não está imprimindo..."
// SAÍDA: "Impressora HP não imprimindo"

// QUANDO USAR: Antes de criar ticket
// BENEFÍCIO: Padroniza títulos, melhora busca no GLPI
```

#### `TicketSummaryBuilderService.java`
```java
// O QUE FAZ: Constrói resumo formatado do chamado
// RETORNA: String com todos os dados formatados
// FORMATO:
// 📋 Resumo do Chamado
// Tipo: Hardware → Impressora
// Descrição: ...
// Local: ...
// etc.

// QUANDO USAR: Estado ConfirmingState
```

#### `UpdatedTicketSummaryBuilderService.java`
```java
// O QUE FAZ: Constrói resumo de atualização de ticket (webhook GLPI)
// FORMATO:
// 🔔 Atualização Chamado #123
// Status: Em atendimento
// Técnico: João Silva
// etc.

// QUANDO USAR: Ao receber webhook do GLPI
```

---

### 📦 `domain/helper/`

Classes auxiliares do domínio.

#### `StateNavigationHelper.java`
```java
// O QUE FAZ: Ajuda navegação entre estados
// MÉTODOS:
// - getPreviousState(current): Retorna estado anterior (comando "voltar")
// - canGoBack(state): Verifica se pode voltar
// - getStatePath(): Retorna caminho de estados

// QUANDO USAR: Comandos globais (voltar, cancelar)
```

---

### 📦 `domain/exception/`

Exceções de domínio.

#### `ConversationException.java`
```java
// O QUE FAZ: Exceção base para erros de conversa
// QUANDO LANÇAR:
// - Estado inválido
// - Transição impossível
// - Dados inconsistentes
```

#### `ValidationException.java`
```java
// O QUE FAZ: Exceção para erros de validação
// QUANDO LANÇAR:
// - Input inválido
// - Formato incorreto
// - Regra de negócio violada
```

---

## 📱 Camada de Aplicação (conversation)

### `conversation/application/`

**Responsabilidade**: Orquestra casos de uso e coordena domínio com infraestrutura.

---

### 📦 `application/facade/`

**Pattern**: Facade - Simplifica interface complexa.

#### `ChatbotFacade.java` (Interface)
```java
// O QUE FAZ: Interface pública do chatbot
// MÉTODO PRINCIPAL:
// - processMessage(phone, message): String

// QUANDO USAR: Ponto de entrada único do sistema
```

#### `ChatbotFacadeImpl.java`
```java
// O QUE FAZ: Implementação do Facade
// RESPONSABILIDADES:
// 1. Recebe mensagem do webhook
// 2. Recupera conversa do Redis
// 3. Delega para ProcessMessageUseCase
// 4. Salva conversa atualizada
// 5. Retorna resposta

// FLUXO:
// Webhook → Facade → Use Case → Domain → Repository → Response

// QUANDO USAR: Controladores chamam apenas o Facade
```

---

### 📦 `application/usecase/`

**Pattern**: Use Case - Um caso de uso específico.

#### `ProcessMessageUseCase.java`
```java
// O QUE FAZ: Processa uma mensagem do usuário
// RESPONSABILIDADES:
// 1. Verifica comandos globais (/help, /cancel, /back)
// 2. Obtém estado atual da conversa
// 3. Processa mensagem no estado
// 4. Atualiza estado da conversa
// 5. Retorna resposta

// QUANDO USAR: Chamado pelo ChatbotFacade
```

#### `CreateTicketUseCase.java`
```java
// O QUE FAZ: Cria ticket no GLPI
// RESPONSABILIDADES:
// 1. Valida dados completos
// 2. Gera título automático (NLP)
// 3. Mapeia categorias para IDs GLPI
// 4. Chama TicketGateway.createTicket()
// 5. Retorna número do ticket

// QUANDO USAR: Estado ConfirmingState (após confirmação)
```

---

### 📦 `application/service/`

Serviços de aplicação.

#### `CompositeGlobalCommandHandler.java`
```java
// O QUE FAZ: Gerencia comandos globais (/help, /cancel, /back)
// PATTERN: Composite
// RESPONSABILIDADE:
// - Detecta comando na mensagem
// - Delega para handler específico
// - Retorna resposta ou null

// QUANDO USAR: Antes de processar estado normal
```

#### `HelpCommandHandler.java`
```java
// O QUE FAZ: Processa comando /help ou "ajuda"
// RETORNA: Mensagem com instruções de uso

// QUANDO USAR: Usuário digita /help
```

#### `CancelCommandHandler.java`
```java
// O QUE FAZ: Processa comando /cancel ou "cancelar"
// COMPORTAMENTO:
// - Reseta conversa
// - Volta para GREETING
// - Limpa dados coletados

// QUANDO USAR: Usuário quer recomeçar
```

#### `BackCommandHandler.java`
```java
// O QUE FAZ: Processa comando /back ou "voltar"
// COMPORTAMENTO:
// - Volta para estado anterior
// - Usa StateNavigationHelper

// QUANDO USAR: Usuário quer corrigir resposta anterior
```

#### `MessageValidationService.java`
```java
// O QUE FAZ: Valida mensagem de entrada
// VERIFICAÇÕES:
// - Não é nula
// - Não é vazia
// - Sanitiza HTML/XSS
// - Remove espaços extras

// QUANDO USAR: Primeira coisa ao receber mensagem
```

#### `FeedbackService.java`
```java
// O QUE FAZ: Gerencia coleta de feedback
// MÉTODOS:
// - requestFeedback(ticketId): Envia solicitação
// - processFeedback(rating, comment): Salva feedback

// QUANDO USAR: Após ticket ser resolvido (webhook GLPI)
```

---

### 📦 `application/port/input/`

**Ports de Entrada**: Interfaces que a aplicação oferece.

#### `StateProcessorPort.java`
```java
// O QUE FAZ: Interface para processar estados
// MÉTODO:
// - process(state, message, conversation): Response

// IMPLEMENTADO POR: ProcessMessageUseCase
```

#### `CategoryMapperPort.java`
```java
// O QUE FAZ: Interface para mapear categorias
// IMPLEMENTADO POR: CategoryMapperService
```

#### `TitleGeneratorPort.java`
```java
// O QUE FAZ: Interface para gerar títulos
// IMPLEMENTADO POR: NlpTitleGeneratorService
```

#### `MessageValidationPort.java`
```java
// O QUE FAZ: Interface para validar mensagens
// IMPLEMENTADO POR: MessageValidationService
```

#### `SummaryBuilderPort.java`
```java
// O QUE FAZ: Interface para construir resumos
// IMPLEMENTADO POR: TicketSummaryBuilderService
```

#### `UrgencyMapperPort.java`
```java
// O QUE FAZ: Interface para mapear urgência
// IMPLEMENTADO POR: UrgencyMapperService
```

#### `GlobalCommandHandler.java`
```java
// O QUE FAZ: Interface para handlers de comandos globais
// IMPLEMENTADO POR: HelpCommandHandler, CancelCommandHandler, etc
```

---

### 📦 `application/port/output/`

**Ports de Saída**: Interfaces que a aplicação precisa (implementadas pela infraestrutura).

#### `ConversationStateRepository.java`
```java
// O QUE FAZ: Interface para persistir conversas
// MÉTODOS:
// - save(conversation): void
// - findByPhone(phone): Optional<ConversationState>
// - delete(phone): void

// IMPLEMENTADO POR: RedisConversationStateRepository (infra)
```

#### `TicketGateway.java`
```java
// O QUE FAZ: Interface para criar tickets
// MÉTODO:
// - createTicket(conversation): ticketId

// IMPLEMENTADO POR: GlpiService (integration)
// BENEFÍCIO: Domínio não depende de GLPI, pode trocar por Jira, ServiceNow, etc
```

---

## 🔧 Camada de Infraestrutura (conversation)

### `conversation/infrastructure/`

**Responsabilidade**: Implementa detalhes técnicos (Redis, cache, métricas, schedulers).

---

### 📦 `infrastructure/adapter/`

Implementações de ports de saída.

#### `RedisConversationStateRepository.java`
```java
// O QUE FAZ: Implementa ConversationStateRepository usando Redis
// MÉTODOS:
// - save(): Serializa ConversationState para JSON e salva no Redis
// - findByPhone(): Busca no Redis e desserializa
// - delete(): Remove do Redis

// CHAVE REDIS: "conversation:{phone}"
// TTL: 30 minutos (configurável)

// QUANDO USAR: Automaticamente usado pelo Facade
```

---

### 📦 `infrastructure/cache/`

Serviços de cache L2 (Caffeine).

#### `SaveConversationCacheService.java`
```java
// O QUE FAZ: Cache local (memória) de conversas
// BENEFÍCIO: Reduz acessos ao Redis
// ESTRATÉGIA: Write-through

// QUANDO USAR: Chamado automaticamente ao salvar conversa
```

#### `GetConversationStateCacheService.java`
```java
// O QUE FAZ: Busca conversa primeiro no cache, depois no Redis
// BENEFÍCIO: Latência ~1ms vs ~10ms do Redis

// QUANDO USAR: Chamado automaticamente ao buscar conversa
```

#### `DeleteConversationCacheService.java`
```java
// O QUE FAZ: Remove conversa do cache e Redis
// QUANDO USAR: Ao completar chamado ou cancelar
```

---

### 📦 `infrastructure/metrics/`

Métricas de observabilidade.

#### `BotMetrics.java`
```java
// O QUE FAZ: Coleta métricas Prometheus
// MÉTRICAS:
// - conversation_total: Total de conversas iniciadas
// - ticket_created_total: Total de tickets criados
// - message_processed_total: Total de mensagens processadas
// - state_transition_total: Transições de estado
// - error_total: Erros por tipo

// QUANDO USAR: Chamado automaticamente em pontos-chave
// ACESSO: http://localhost:8082/actuator/prometheus
```

---

### 📦 `infrastructure/scheduler/`

Tarefas agendadas.

#### `InactivityTimeoutScheduler.java`
```java
// O QUE FAZ: Remove conversas inativas
// FREQUÊNCIA: A cada 5 minutos
// LÓGICA:
// - Busca conversas no Redis
// - Verifica lastUpdate > 30 minutos
// - Remove conversas expiradas

// BENEFÍCIO: Libera memória do Redis
```

---

## 🔌 Integrações Externas (integration)

### `integration/`

**Responsabilidade**: Integrações com sistemas externos (Evolution API, GLPI).

---

## 📱 Evolution API

### `integration/evolution/`

Integração com Evolution API (Gateway WhatsApp).

---

### 📦 `evolution/dto/`

Data Transfer Objects para Evolution API.

#### `WebhookEvent.java`
```java
// O QUE FAZ: Representa payload do webhook Evolution
// CAMPOS:
// - event: Tipo do evento ("messages.upsert")
// - instance: Nome da instância
// - data: Dados da mensagem
//   - key.remoteJid: Telefone (5511999999999@s.whatsapp.net)
//   - key.id: ID único da mensagem
//   - message.conversation: Texto da mensagem

// QUANDO USAR: EvolutionWebhookController desserializa isso
```

#### `SendMessageRequest.java`
```java
// O QUE FAZ: Payload para enviar mensagem
// CAMPOS:
// - number: Telefone destino
// - textMessage.text: Texto da mensagem

// QUANDO USAR: Ao enviar resposta para usuário
```

#### `SendMessageResponse.java`
```java
// O QUE FAZ: Resposta da API ao enviar mensagem
// CAMPOS:
// - messageId: ID da mensagem enviada
// - status: Status do envio

// QUANDO USAR: Para confirmar envio
```

#### `MessageResponse.java`
```java
// O QUE FAZ: Resposta genérica da API
```

#### `DataListDeserializer.java`
```java
// O QUE FAZ: Deserializador customizado Jackson
// PROBLEMA: Evolution retorna data como array OU objeto
// SOLUÇÃO: Trata ambos os casos

// QUANDO USAR: Automaticamente ao desserializar WebhookEvent
```

---

### 📦 Principais Classes Evolution

#### `EvolutionPropertiesClient.java`
```java
// O QUE FAZ: Carrega propriedades da Evolution do application.properties
// PROPRIEDADES:
// - evolution.api.url
// - evolution.api.key
// - evolution.api.instance
```

#### `EvolutionClient.java`
```java
// O QUE FAZ: Cliente HTTP para Evolution API
// MÉTODOS:
// - sendMessage(phone, text): Envia mensagem WhatsApp
// - sendTextMessage(request): Versão genérica

// TECNOLOGIA: WebClient (reativo)
// TIMEOUT: 3 segundos
// RETRY: 3 tentativas com backoff exponencial
```

#### `EvolutionService.java`
```java
// O QUE FAZ: Camada de serviço sobre EvolutionClient
// RESPONSABILIDADES:
// - Valida dados antes de enviar
// - Trata erros
// - Loga operações
// - Aplica circuit breaker

// CIRCUIT BREAKER:
// - Abre após 50% de falhas
// - Wait: 20 segundos
// - Protege contra API Evolution fora do ar

// QUANDO USAR: ChatbotFacade chama para enviar respostas
```

---

### 📦 `evolution/webhook/`

Controller que recebe webhooks da Evolution.

#### `EvolutionWebhookController.java`
```java
// O QUE FAZ: Recebe mensagens do WhatsApp via webhook
// ENDPOINT: POST /api/webhook/evolution
// FLUXO:
// 1. Recebe payload JSON
// 2. Valida evento (filtra apenas messages.upsert)
// 3. Verifica idempotência (messageId)
// 4. Extrai telefone e mensagem
// 5. Chama ChatbotFacade.processMessage()
// 6. Envia resposta via EvolutionService

// IDEMPOTÊNCIA: Previne processar mensagem duplicada
// HEALTH CHECK: GET /api/webhook/evolution/health
```

---

### 📦 `evolution/exception/`

#### `EvolutionApiException.java`
```java
// O QUE FAZ: Exceção para erros da Evolution API
// QUANDO LANÇAR:
// - Timeout
// - Erro HTTP 4xx/5xx
// - Resposta inválida
```

---

## 🎫 GLPI Integration

### `integration/glpi/`

Integração com GLPI (Sistema ITSM).

---

### 📦 `glpi/dto/`

DTOs para GLPI API.

#### `CreateTicketRequest.java`
```java
// O QUE FAZ: Payload para criar ticket
// CAMPOS:
// - name: Título do ticket
// - content: Descrição
// - type: Tipo (1=Incidente, 2=Requisição)
// - urgency: Urgência (1-4)
// - itilcategories_id: ID da categoria
// - _users_id_requester: ID do usuário solicitante
// - location: Localização
// - phone: Telefone

// QUANDO USAR: CreateTicketUseCase monta isso
```

#### `CreateTicketResponse.java`
```java
// O QUE FAZ: Resposta ao criar ticket
// CAMPOS:
// - id: ID do ticket criado
// - message: Mensagem de sucesso/erro
```

#### `GlpiSessionResponse.java`
```java
// O QUE FAZ: Resposta de autenticação
// CAMPO:
// - session_token: Token JWT da sessão

// QUANDO USAR: Login no GLPI
```

---

### 📦 `glpi/enums/`

#### `GlpiTicketStatus.java`
```java
// O QUE FAZ: Status de ticket no GLPI
// VALORES:
// - NEW (1): Novo
// - ASSIGNED (2): Atribuído
// - PROCESSING (3): Em atendimento
// - PENDING (4): Pendente
// - SOLVED (5): Resolvido
// - CLOSED (6): Fechado
```

#### `GlpiTicketType.java`
```java
// O QUE FAZ: Tipos de ticket
// VALORES:
// - INCIDENT (1): Incidente
// - REQUEST (2): Requisição
```

#### `GlpiUserType.java`
```java
// O QUE FAZ: Tipos de usuário
```

---

### 📦 Principais Classes GLPI

#### `GlpiPropertiesClient.java`
```java
// O QUE FAZ: Carrega propriedades do GLPI
// PROPRIEDADES:
// - glpi.api.url
// - glpi.api.app.token
// - glpi.api.user.token
```

#### `GlpiSessionManager.java`
```java
// O QUE FAZ: Gerencia sessão/autenticação GLPI
// MÉTODOS:
// - initSession(): Cria sessão, retorna token
// - killSession(token): Encerra sessão

// CACHE: Token fica em cache por 8 horas
// QUANDO USAR: Antes de cada operação no GLPI
```

#### `GlpiClient.java`
```java
// O QUE FAZ: Cliente HTTP para GLPI API
// MÉTODOS:
// - initSession(): POST /initSession
// - createTicket(request): POST /Ticket
// - getTicket(id): GET /Ticket/{id}
// - killSession(): GET /killSession

// HEADERS:
// - App-Token: Token da aplicação
// - Session-Token: Token da sessão
// - Content-Type: application/json

// TECNOLOGIA: RestTemplate
// TIMEOUT: 5 segundos
```

#### `GlpiService.java`
```java
// O QUE FAZ: Camada de serviço sobre GlpiClient
// MÉTODOS:
// - createTicket(conversation): String (ticketId)
// - getUserPhone(userId): String
// - searchUser(name): User

// RESPONSABILIDADES:
// - Gerencia sessão automaticamente
// - Converte ConversationState → CreateTicketRequest
// - Mapeia categorias (via CategoryMapperService)
// - Trata erros
// - Aplica circuit breaker

// CIRCUIT BREAKER:
// - Abre após 50% de falhas
// - Wait: 30 segundos

// QUANDO USAR: CreateTicketUseCase chama para criar ticket
```

#### `GlpiSearch.java`
```java
// O QUE FAZ: Busca avançada no GLPI
// MÉTODOS:
// - searchTicketByPhone(phone): Busca tickets por telefone
// - searchUser(criteria): Busca usuários

// TECNOLOGIA: GLPI Search API
```

#### `GlpiAttribution.java`
```java
// O QUE FAZ: Atribui ticket a técnico
// QUANDO USAR: Após criar ticket (opcional)
```

#### `GlpiTicketPayload.java`
```java
// O QUE FAZ: Builder para payload de ticket
// FACILITA: Construção de payloads complexos
```

---

### 📦 `glpi/webhook/`

Recebe notificações do GLPI.

#### `GlpiWebhookEvent.java`
```java
// O QUE FAZ: Payload do webhook GLPI
// CAMPOS:
// - ticketId: ID do ticket
// - eventType: Tipo de evento (TICKET_ASSIGNED, TICKET_RESOLVED, etc)
// - status: Status atual
// - assignedTo: Técnico responsável
// - phone: Telefone do usuário
// - message: Mensagem da notificação
```

#### `GlpiWebhookController.java`
```java
// O QUE FAZ: Recebe notificações de mudanças em tickets
// ENDPOINT: POST /api/webhook/glpi/notification
// FLUXO:
// 1. Recebe evento do GLPI
// 2. Verifica idempotência (ticketId + eventType)
// 3. Chama GlpiWebhookService.processWebhookEvent()

// EVENTOS SUPORTADOS:
// - TICKET_ASSIGNED: Ticket atribuído a técnico
// - TICKET_IN_PROGRESS: Ticket em atendimento
// - TICKET_RESOLVED: Ticket resolvido
// - TICKET_CLOSED: Ticket fechado
// - COMMENT_ADDED: Comentário adicionado

// HEALTH CHECK: GET /api/webhook/glpi/health
```

#### `GlpiWebhookService.java`
```java
// O QUE FAZ: Processa eventos do webhook GLPI
// RESPONSABILIDADES:
// - Constrói mensagem de notificação
// - Envia notificação via EvolutionService
// - Solicita feedback (se ticket resolvido)

// ASSÍNCRONO: @Async
// QUANDO USAR: Chamado pelo GlpiWebhookController
```

---

### 📦 `glpi/exception/`

#### `GlpiAuthenticationException.java`
```java
// O QUE FAZ: Erro de autenticação
// QUANDO: Token inválido, sessão expirada
```

#### `GlpiTicketCreationException.java`
```java
// O QUE FAZ: Erro ao criar ticket
// QUANDO: Dados inválidos, categoria não existe, etc
```

#### `GlpiInvalidResponseException.java`
```java
// O QUE FAZ: Resposta inválida da API
// QUANDO: JSON malformado, campos faltando
```

---

## 🔧 Shared (Código Compartilhado)

### `shared/`

**Responsabilidade**: Código compartilhado entre todos os módulos.

---

### 📦 `shared/config/`

Configurações Spring.

#### `RedisConfig.java`
```java
// O QUE FAZ: Configura conexão Redis
// CONFIGURAÇÕES:
// - RedisConnectionFactory
// - RedisTemplate
// - Serialização JSON

// PROPRIEDADES:
// - spring.data.redis.host
// - spring.data.redis.port
// - spring.data.redis.password
```

#### `CacheConfig.java`
```java
// O QUE FAZ: Configura cache L2 (Caffeine)
// ESPECIFICAÇÕES:
// - Máximo 1000 entradas
// - Expiração: 30 minutos
// - Eviction: LRU

// BENEFÍCIO: Cache local reduz latência
```

#### `RestTemplateConfig.java`
```java
// O QUE FAZ: Configura RestTemplate para HTTP
// CONFIGURAÇÕES:
// - Timeout conexão: 5s
// - Timeout leitura: 10s
// - Interceptors de logging

// USADO POR: GlpiClient
```

#### `RateLimitConfig.java`
```java
// O QUE FAZ: Configura rate limiting
// ESTRATÉGIA: Token bucket
// LIMITES:
// - 10 requisições/segundo por IP
// - 100 requisições/minuto por IP

// TECNOLOGIA: Resilience4j RateLimiter
```

#### `ChatbotProperties.java`
```java
// O QUE FAZ: Carrega propriedades customizadas
// PROPRIEDADES:
// - chatbot.conversation.ttl-minutes
// - chatbot.validation.title.min-length
// - chatbot.validation.description.min-length
// - chatbot.redis.key-prefix
```

#### `OpenApiConfig.java`
```java
// O QUE FAZ: Configura Swagger/OpenAPI
// ESPECIFICAÇÕES:
// - Título da API
// - Descrição
// - Versão
// - Contato
// - Servers

// ACESSO: http://localhost:8082/swagger-ui.html
```

---

### 📦 `shared/exception/`

Exceções e tratamento global.

#### `BusinessException.java`
```java
// O QUE FAZ: Exceção base para erros de negócio
// CAMPOS:
// - message: Mensagem de erro
// - code: Código do erro

// QUANDO LANÇAR: Regra de negócio violada
```

#### `AdvancedGlobalExceptionHandler.java`
```java
// O QUE FAZ: Trata exceções globalmente (@ControllerAdvice)
// EXCEÇÕES TRATADAS:
// - ValidationException → 400 Bad Request
// - BusinessException → 422 Unprocessable Entity
// - GlpiAuthenticationException → 401 Unauthorized
// - Exception genérica → 500 Internal Server Error

// BENEFÍCIO: Respostas padronizadas de erro
```

---

### 📦 `shared/dto/`

#### `AdvancedErrorResponseDTO.java`
```java
// O QUE FAZ: Formato padronizado de erro
// CAMPOS:
// - timestamp: Data/hora do erro
// - status: HTTP status code
// - error: Nome do erro
// - message: Mensagem descritiva
// - path: Path da requisição
// - traceId: ID para rastreamento

// RETORNADO POR: AdvancedGlobalExceptionHandler
```

---

### 📦 `shared/idempotency/`

#### `IdempotencyService.java`
```java
// O QUE FAZ: Garante processamento exactly-once
// MÉTODO:
// - tryAcquire(key): boolean
//   - true: Primeira vez, pode processar
//   - false: Já processou, duplicado

// IMPLEMENTAÇÃO:
// - Usa Redis SET com NX (set if not exists)
// - TTL: 24 horas

// CHAVES:
// - webhook:evolution:{messageId}
// - webhook:glpi:{ticketId}:{eventType}

// QUANDO USAR: Antes de processar webhooks
```

---

### 📦 `shared/ratelimit/`

#### `RateLimitInterceptor.java`
```java
// O QUE FAZ: Interceptor HTTP para rate limiting
// LÓGICA:
// - Extrai IP da requisição
// - Verifica limite (RateLimiter)
// - Se exceder → 429 Too Many Requests
// - Se ok → Processa

// QUANDO: Automaticamente em todas as requisições
```

---

### 📦 `shared/util/`

Utilitários.

#### `InputSanitizer.java`
```java
// O QUE FAZ: Sanitiza entrada do usuário
// PROTEÇÕES:
// - Remove HTML tags
// - Escapa SQL injection
// - Remove caracteres especiais perigosos
// - Limita tamanho

// QUANDO USAR: Antes de salvar qualquer input
```

#### `StackTraceUtil.java`
```java
// O QUE FAZ: Utilitário para stack traces
// MÉTODOS:
// - getStackTraceAsString(exception): String

// QUANDO USAR: Logging de erros
```

#### `TitleGenerator.java`
```java
// O QUE FAZ: Gera título de ticket (versão simples)
// LÓGICA:
// - Pega primeiras palavras da descrição
// - Limita a 100 caracteres

// QUANDO USAR: Fallback do NlpTitleGeneratorService
```

#### `TituloNaturalPTBR.java`
```java
// O QUE FAZ: Processa texto em português
// TÉCNICAS:
// - Remoção de stopwords
// - Extração de keywords
// - Capitalização

// USADO POR: NlpTitleGeneratorService
```

---

## 📱 API Layer

### `conversation/api/`

Controllers REST (se houver API REST além de webhooks).

#### `ChatbotWebhookController.java`
```java
// O QUE FAZ: Controller REST para interação direta (opcional)
// ENDPOINTS:
// - POST /api/chatbot/message: Envia mensagem programaticamente
// - GET /api/chatbot/conversation/{phone}: Busca estado de conversa

// QUANDO USAR: Testes, integrações customizadas
```

---

## 🌊 Fluxo de Dados

### Fluxo Completo: Usuário Abre Chamado

```
1. Usuário envia "oi" no WhatsApp
   ↓
2. Evolution API recebe mensagem
   ↓
3. Evolution envia webhook → EvolutionWebhookController
   ↓
4. Controller valida e extrai dados (phone, message)
   ↓
5. Controller verifica idempotência (IdempotencyService)
   ↓
6. Controller chama → ChatbotFacade.processMessage(phone, message)
   ↓
7. Facade busca conversa → RedisConversationStateRepository.findByPhone()
   ↓
8. Se não existe → Cria nova (estado GREETING)
   ↓
9. Facade chama → ProcessMessageUseCase.execute()
   ↓
10. Use Case verifica comandos globais (CompositeGlobalCommandHandler)
    ↓
11. Use Case obtém estado atual (ex: GreetingState)
    ↓
12. Use Case chama → GreetingState.processMessage(message, conversation)
    ↓
13. Estado processa, atualiza conversa, retorna resposta
    ↓
14. Use Case salva conversa → RedisConversationStateRepository.save()
    ↓
15. Facade retorna resposta ao Controller
    ↓
16. Controller envia resposta → EvolutionService.sendMessage()
    ↓
17. EvolutionClient envia HTTP → Evolution API
    ↓
18. Evolution API envia → WhatsApp do usuário
```

### Fluxo: Criar Ticket no GLPI

```
1. Usuário confirma dados (estado CONFIRMING)
   ↓
2. ConfirmingState detecta "sim"
   ↓
3. State chama → CreateTicketUseCase.execute(conversation)
   ↓
4. Use Case gera título → NlpTitleGeneratorService.generate()
   ↓
5. Use Case mapeia categoria → CategoryMapperService.map()
   ↓
6. Use Case mapeia urgência → UrgencyMapperService.map()
   ↓
7. Use Case monta payload → CreateTicketRequest
   ↓
8. Use Case chama → TicketGateway.createTicket() (interface)
   ↓
9. GlpiService (implementa TicketGateway) recebe chamada
   ↓
10. GlpiService obtém sessão → GlpiSessionManager.initSession()
    ↓
11. GlpiService chama → GlpiClient.createTicket(request)
    ↓
12. GlpiClient envia HTTP → GLPI API
    ↓
13. GLPI cria ticket, retorna ID
    ↓
14. GlpiClient retorna response
    ↓
15. GlpiService extrai ID do ticket
    ↓
16. Use Case retorna ticketId
    ↓
17. ConfirmingState atualiza conversation.ticketId
    ↓
18. ConfirmingState transiciona → CompletedState
    ↓
19. CompletedState retorna mensagem "✅ Chamado #123 criado!"
```

### Fluxo: Webhook GLPI (Notificação)

```
1. Técnico atribui ticket no GLPI
   ↓
2. GLPI (ou script cronjob) envia webhook → GlpiWebhookController
   ↓
3. Controller valida idempotência
   ↓
4. Controller chama → GlpiWebhookService.processWebhookEvent(event)
   ↓
5. Service constrói mensagem → UpdatedTicketSummaryBuilderService
   ↓
6. Service envia notificação → EvolutionService.sendMessage(phone, message)
   ↓
7. EvolutionClient → Evolution API → WhatsApp
   ↓
8. Usuário recebe: "🔔 Seu chamado foi atribuído ao técnico João Silva"
```

---

## 🔗 Diagrama de Dependências

### Camada de Domínio (Núcleo)
```
┌─────────────────────────────────────────┐
│          DOMAIN LAYER                   │
│  (Não depende de nada externo)          │
│                                         │
│  ┌──────────┐  ┌──────────────┐        │
│  │ Entities │  │    States    │        │
│  └──────────┘  └──────────────┘        │
│       │               │                 │
│       ▼               ▼                 │
│  ┌─────────────────────────┐           │
│  │   Domain Services       │           │
│  │ - CategoryMapper        │           │
│  │ - TitleGenerator (NLP)  │           │
│  │ - SummaryBuilder        │           │
│  └─────────────────────────┘           │
│       │                                 │
│       ▼                                 │
│  ┌──────────┐                           │
│  │Validators│                           │
│  └──────────┘                           │
└─────────────────────────────────────────┘
```

### Camada de Aplicação (Orquestração)
```
┌─────────────────────────────────────────┐
│       APPLICATION LAYER                 │
│   (Depende apenas do Domínio)           │
│                                         │
│  ┌──────────────────┐                   │
│  │  ChatbotFacade   │ ← Ponto de entrada│
│  └────────┬─────────┘                   │
│           │                             │
│           ▼                             │
│  ┌─────────────────┐                    │
│  │   Use Cases     │                    │
│  │ - ProcessMessage│                    │
│  │ - CreateTicket  │                    │
│  └────────┬────────┘                    │
│           │                             │
│           ▼                             │
│  ┌─────────────────┐                    │
│  │    Services     │                    │
│  │ - CommandHandler│                    │
│  │ - Validation    │                    │
│  └─────────────────┘                    │
│                                         │
│  ┌─────────────────────────────┐        │
│  │   Ports (Interfaces)        │        │
│  │ Input ↑  │  Output ↓        │        │
│  └─────────────────────────────┘        │
└─────────────────────────────────────────┘
```

### Camada de Infraestrutura (Detalhes Técnicos)
```
┌─────────────────────────────────────────┐
│     INFRASTRUCTURE LAYER                │
│ (Implementa Ports de Saída)             │
│                                         │
│  ┌──────────────────────────┐           │
│  │  Redis Repository        │           │
│  │  (ConversationState)     │           │
│  └──────────────────────────┘           │
│                                         │
│  ┌──────────────────────────┐           │
│  │  Cache Services          │           │
│  │  (Caffeine L2)           │           │
│  └──────────────────────────┘           │
│                                         │
│  ┌──────────────────────────┐           │
│  │  Metrics                 │           │
│  │  (Prometheus)            │           │
│  └──────────────────────────┘           │
│                                         │
│  ┌──────────────────────────┐           │
│  │  Schedulers              │           │
│  │  (Timeout cleanup)       │           │
│  └──────────────────────────┘           │
└─────────────────────────────────────────┘
```

### Camada de Integração (Sistemas Externos)
```
┌─────────────────────────────────────────┐
│      INTEGRATION LAYER                  │
│  (Adapters para sistemas externos)      │
│                                         │
│  ┌──────────────────┐ ┌──────────────┐ │
│  │ Evolution API    │ │   GLPI API   │ │
│  ├──────────────────┤ ├──────────────┤ │
│  │ - Client (HTTP)  │ │ - Client     │ │
│  │ - Service        │ │ - Service    │ │
│  │ - DTOs           │ │ - DTOs       │ │
│  │ - Webhook Ctrl   │ │ - Webhook    │ │
│  └──────────────────┘ └──────────────┘ │
└─────────────────────────────────────────┘
```

### Shared (Transversal)
```
┌─────────────────────────────────────────┐
│           SHARED LAYER                  │
│  (Usado por todas as camadas)           │
│                                         │
│  ┌─────────────────────────────┐        │
│  │  Config                     │        │
│  │  - Redis, Cache, OpenAPI    │        │
│  └─────────────────────────────┘        │
│                                         │
│  ┌─────────────────────────────┐        │
│  │  Exception Handling         │        │
│  │  - Global Handler           │        │
│  └─────────────────────────────┘        │
│                                         │
│  ┌─────────────────────────────┐        │
│  │  Cross-cutting Concerns     │        │
│  │  - Idempotency              │        │
│  │  - Rate Limiting            │        │
│  │  - Input Sanitizer          │        │
│  └─────────────────────────────┘        │
└─────────────────────────────────────────┘
```

---

## 🎯 Resumo por Responsabilidade

### Quando mexer em cada camada:

| Se você quer... | Mexa em... |
|----------------|------------|
| **Adicionar novo estado na conversa** | `domain/state/` + `StateEnum` |
| **Mudar validação de campo** | `domain/validator/` |
| **Adicionar nova categoria** | `domain/service/CategoryMapperService` |
| **Mudar regra de negócio** | `domain/service/` |
| **Adicionar novo comando (/comando)** | `application/service/` (novo Handler) |
| **Adicionar novo caso de uso** | `application/usecase/` |
| **Mudar persistência** | `infrastructure/adapter/` |
| **Adicionar métrica** | `infrastructure/metrics/BotMetrics` |
| **Integrar novo sistema** | `integration/` (novo pacote) |
| **Mudar mensagens do bot** | `messages.properties` |
| **Configurar timeout/pool** | `shared/config/` |
| **Adicionar nova exceção** | `shared/exception/` |

---

## 📝 Convenções de Código

### Nomenclatura

- **Entities**: Substantivo singular (ConversationState, Ticket)
- **Services**: Substantivo + Service (GlpiService, EvolutionService)
- **Use Cases**: Verbo + UseCase (ProcessMessageUseCase, CreateTicketUseCase)
- **Ports**: Substantivo + Port (TicketGateway, CategoryMapperPort)
- **States**: Gerúndio + State (CollectingCategoryState, ConfirmingState)
- **Validators**: Campo + Validator (DescriptionValidator, RamalValidator)
- **DTOs**: Substantivo + Request/Response/Event (WebhookEvent, CreateTicketRequest)

### Packages

- `domain`: Lógica de negócio pura
- `application`: Orquestração e casos de uso
- `infrastructure`: Detalhes técnicos (Redis, cache, etc)
- `integration`: Sistemas externos (Evolution, GLPI)
- `shared`: Código compartilhado
- `api`: Controllers REST

### Dependências

```
domain → (nada)
application → domain
infrastructure → domain, application
integration → domain, application
shared → (nada ou tudo)
api → application
```

---

## 🔍 Como Encontrar o Que Você Precisa

### "Quero mudar a mensagem de boas-vindas"
→ `domain/state/GreetingState.java`

### "Quero adicionar nova validação de campo"
→ `domain/validator/` (criar novo Validator)

### "Quero mudar TTL das conversas"
→ `application.properties` → `chatbot.conversation.ttl-minutes`

### "Quero adicionar novo tipo de urgência"
→ `domain/service/UrgencyMapperService.java`

### "Quero ver métricas"
→ `http://localhost:8082/actuator/prometheus`

### "Quero testar API"
→ `http://localhost:8082/swagger-ui.html`

### "Webhook não está chegando"
→ `integration/evolution/webhook/EvolutionWebhookController.java` (debugar aqui)

### "Ticket não é criado no GLPI"
→ `integration/glpi/GlpiService.java` (debugar aqui)

### "Redis não conecta"
→ `shared/config/RedisConfig.java`

### "Conversa não é salva"
→ `infrastructure/adapter/RedisConversationStateRepository.java`

---

## 📚 Referências Rápidas

### Principais Classes para Começar

1. **ChatbotFacadeImpl** - Ponto de entrada do sistema
2. **ProcessMessageUseCase** - Processa qualquer mensagem
3. **GreetingState** - Primeiro estado da conversa
4. **GlpiService** - Cria tickets no GLPI
5. **EvolutionService** - Envia mensagens WhatsApp

### Principais Configurações

- `application.properties` - Todas as configurações
- `messages.properties` - Mensagens do bot
- `docker-compose.yml` - Infraestrutura
- `.env` - Variáveis de ambiente

### Principais Endpoints

- `POST /api/webhook/evolution` - Recebe mensagens
- `POST /api/webhook/glpi/notification` - Recebe notificações
- `GET /actuator/health` - Health check
- `GET /actuator/prometheus` - Métricas
- `GET /swagger-ui.html` - Documentação API

---

**Este documento é seu mapa do tesouro do projeto! 🗺️**

Sempre que esquecer onde algo está, volte aqui! 📌
