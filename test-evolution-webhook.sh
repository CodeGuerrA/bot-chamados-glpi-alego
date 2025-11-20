#!/bin/bash

# Script de teste para webhook Evolution com HMAC
# Testa tanto assinatura válida quanto inválida

# Cores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configurações
WEBHOOK_URL="http://localhost:8082/api/webhook/evolution"
SECRET="7225f25357a4dd9162c6eeebcc857a8ad30f23c18d6fcdd8401e59376c35e8fd"

echo "========================================="
echo "Teste de Webhook Evolution com HMAC"
echo "========================================="
echo ""

# Teste 1: Webhook com assinatura válida
echo -e "${YELLOW}Teste 1: Webhook com assinatura VÁLIDA${NC}"
PAYLOAD='{"event":"messages.upsert","instance":"chatbot","data":{"key":{"remoteJid":"5511999999999@s.whatsapp.net","fromMe":false,"id":"ABC123TEST"},"message":{"conversation":"Teste de webhook com HMAC"}}}'

# Calcula assinatura HMAC-SHA256
SIGNATURE=$(echo -n "$PAYLOAD" | openssl dgst -sha256 -hmac "$SECRET" | awk '{print $2}')

echo "Payload: $PAYLOAD"
echo "Signature: $SIGNATURE"
echo ""

# Envia webhook
RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$WEBHOOK_URL" \
  -H "Content-Type: application/json" \
  -H "X-Webhook-Signature: $SIGNATURE" \
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

# Teste 2: Webhook com assinatura inválida
echo -e "${YELLOW}Teste 2: Webhook com assinatura INVÁLIDA${NC}"
INVALID_SIGNATURE="assinatura_invalida_123"

echo "Payload: $PAYLOAD"
echo "Signature: $INVALID_SIGNATURE"
echo ""

RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$WEBHOOK_URL" \
  -H "Content-Type: application/json" \
  -H "X-Webhook-Signature: $INVALID_SIGNATURE" \
  -d "$PAYLOAD")

HTTP_CODE=$(echo "$RESPONSE" | grep "HTTP_CODE:" | cut -d':' -f2)
BODY=$(echo "$RESPONSE" | sed '/HTTP_CODE:/d')

if [ "$HTTP_CODE" = "401" ]; then
    echo -e "${GREEN}✓ SUCESSO: Rejeitado corretamente (HTTP $HTTP_CODE)${NC}"
else
    echo -e "${RED}✗ FALHA: Deveria retornar 401, mas retornou HTTP $HTTP_CODE${NC}"
fi

echo ""
echo "========================================="
echo ""

# Teste 3: Webhook sem assinatura
echo -e "${YELLOW}Teste 3: Webhook SEM assinatura${NC}"

RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$WEBHOOK_URL" \
  -H "Content-Type: application/json" \
  -d "$PAYLOAD")

HTTP_CODE=$(echo "$RESPONSE" | grep "HTTP_CODE:" | cut -d':' -f2)
BODY=$(echo "$RESPONSE" | sed '/HTTP_CODE:/d')

echo "HTTP Code: $HTTP_CODE"
echo "Response: $BODY"

if [ "$HTTP_CODE" = "200" ]; then
    echo -e "${YELLOW}⚠ AVISO: Webhook aceito sem assinatura (Evolution API não suporta nativamente)${NC}"
else
    echo -e "${GREEN}✓ Webhook rejeitado${NC}"
fi

echo ""
echo "========================================="
echo "Testes concluídos!"
echo "========================================="
