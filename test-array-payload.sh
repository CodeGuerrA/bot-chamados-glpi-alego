#!/bin/bash

# Teste de webhook com campo 'data' como array

SECRET="7225f25357a4dd9162c6eeebcc857a8ad30f23c18d6fcdd8401e59376c35e8fd"
PAYLOAD='{"event":"messages.upsert","instance":"chatbot","data":[{"key":{"remoteJid":"5511888888888@s.whatsapp.net","fromMe":false,"id":"ARRAY_TEST_123"},"message":{"conversation":"Teste com data como array"}}]}'

# Calcula assinatura
SIGNATURE=$(echo -n "$PAYLOAD" | openssl dgst -sha256 -hmac "$SECRET" | awk '{print $2}')

echo "Testando webhook com campo 'data' como ARRAY"
echo "Payload: $PAYLOAD"
echo ""

# Envia webhook
RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "http://localhost:8082/api/webhook/evolution" \
  -H "Content-Type: application/json" \
  -H "X-Webhook-Signature: $SIGNATURE" \
  -d "$PAYLOAD")

HTTP_CODE=$(echo "$RESPONSE" | grep "HTTP_CODE:" | cut -d':' -f2)
BODY=$(echo "$RESPONSE" | sed '/HTTP_CODE:/d')

echo "HTTP Code: $HTTP_CODE"
echo "Response: $BODY"
