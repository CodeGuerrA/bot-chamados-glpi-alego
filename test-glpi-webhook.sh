#!/bin/bash

# Script de teste para webhook GLPI (sem autenticação)

# Cores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configurações
WEBHOOK_URL="http://localhost:8082/api/webhook/glpi/notification"

echo "========================================="
echo "Teste de Webhook GLPI"
echo "========================================="
echo ""

# Teste 1: Webhook normal
echo -e "${YELLOW}Teste 1: Webhook com notificação válida${NC}"
PAYLOAD='{"ticketId":123,"eventType":"TICKET_ASSIGNED","status":"Em atendimento","assignedTo":"João Silva","phone":"5511999999999","message":"Seu chamado #123 foi atribuído ao técnico João Silva"}'

echo "Payload: $PAYLOAD"
echo ""

# Envia webhook
RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$WEBHOOK_URL" \
  -H "Content-Type: application/json" \
  -d "$PAYLOAD")

HTTP_CODE=$(echo "$RESPONSE" | grep "HTTP_CODE:" | cut -d':' -f2)
BODY=$(echo "$RESPONSE" | sed '/HTTP_CODE:/d')

if [ "$HTTP_CODE" = "200" ]; then
    echo -e "${GREEN}✓ SUCESSO: $BODY (HTTP $HTTP_CODE)${NC}"
else
    echo -e "${RED}✗ FALHA: $BODY (HTTP $HTTP_CODE)${NC}"
fi

echo ""
echo "========================================="
echo ""

# Teste 2: Teste de idempotência (mesma mensagem 2x)
echo -e "${YELLOW}Teste 2: Idempotência - enviando mesma mensagem 2x${NC}"

echo "Primeira tentativa:"
RESPONSE1=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$WEBHOOK_URL" \
  -H "Content-Type: application/json" \
  -d "$PAYLOAD")

HTTP_CODE1=$(echo "$RESPONSE1" | grep "HTTP_CODE:" | cut -d':' -f2)
BODY1=$(echo "$RESPONSE1" | sed '/HTTP_CODE:/d')
echo "Response: $BODY1 (HTTP $HTTP_CODE1)"

sleep 1

echo ""
echo "Segunda tentativa (mesma mensagem):"
RESPONSE2=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$WEBHOOK_URL" \
  -H "Content-Type: application/json" \
  -d "$PAYLOAD")

HTTP_CODE2=$(echo "$RESPONSE2" | grep "HTTP_CODE:" | cut -d':' -f2)
BODY2=$(echo "$RESPONSE2" | sed '/HTTP_CODE:/d')
echo "Response: $BODY2 (HTTP $HTTP_CODE2)"

if [[ "$BODY2" == *"Duplicate"* ]] || [[ "$BODY2" == *"duplicado"* ]]; then
    echo -e "${GREEN}✓ SUCESSO: Idempotência funcionando!${NC}"
else
    echo -e "${YELLOW}⚠ Pode não estar funcionando a idempotência${NC}"
fi

echo ""
echo "========================================="
echo "Testes concluídos!"
echo "========================================="
