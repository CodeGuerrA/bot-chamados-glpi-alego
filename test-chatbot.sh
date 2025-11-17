#!/bin/sh

# Script para testar o chatbot via curl
# Uso: ./test-chatbot.sh

BASE_URL="${BASE_URL:-http://localhost:8082}"
PHONE="${PHONE:-5511999999999}"

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "=== Teste do Chatbot GLPI ==="
echo "URL: $BASE_URL"
echo "Phone: $PHONE"
echo ""

# Função para enviar mensagem
send_message() {
    message="$1"
    echo "${YELLOW}Enviando:${NC} $message"

    response=$(curl -s -X POST "$BASE_URL/api/webhook/message" \
        -H "Content-Type: application/json" \
        -d "{\"phone\": \"$PHONE\", \"message\": \"$message\"}")

    if [ $? -eq 0 ]; then
        echo "${GREEN}Resposta:${NC}"
        echo "$response" | jq -r '.message // .' 2>/dev/null || echo "$response"
    else
        echo "${RED}Erro na requisição${NC}"
    fi
    echo "---"
}

# Função para verificar saúde
check_health() {
    echo "${YELLOW}Verificando saúde do serviço...${NC}"
    response=$(curl -s -w "\nHTTP_CODE:%{http_code}" "$BASE_URL/api/webhook/health")
    http_code=$(echo "$response" | grep "HTTP_CODE:" | cut -d: -f2)
    body=$(echo "$response" | grep -v "HTTP_CODE:")

    if [ "$http_code" = "200" ]; then
        echo "${GREEN}Serviço OK${NC}"
    else
        echo "${RED}Serviço indisponível (HTTP $http_code)${NC}"
        exit 1
    fi
    echo "---"
}

# Função para cancelar conversa
cancel_conversation() {
    echo "${YELLOW}Cancelando conversa...${NC}"
    response=$(curl -s -X DELETE "$BASE_URL/api/webhook/conversation/$PHONE")
    echo "${GREEN}Resposta:${NC}"
    echo "$response" | jq -r '.message // .' 2>/dev/null || echo "$response"
    echo "---"
}

# Menu interativo
interactive_mode() {
    echo "Modo interativo - Digite suas mensagens (ou 'sair' para terminar)"
    echo ""

    while true; do
        printf "Você: "
        read -r input

        case "$input" in
            "sair"|"exit"|"quit")
                echo "Encerrando..."
                break
                ;;
            "/cancelar")
                cancel_conversation
                ;;
            "/health")
                check_health
                ;;
            *)
                send_message "$input"
                ;;
        esac
    done
}

# Teste automatizado de fluxo completo
auto_test() {
    echo "=== Teste Automatizado de Fluxo Completo ==="
    echo ""

    # Cancela conversa anterior se existir
    cancel_conversation

    # Inicia conversa
    send_message "oi"
    sleep 1

    # Envia descrição
    send_message "Computador não liga"
    sleep 1

    # Envia local
    send_message "Sala 101 - Bloco A"
    sleep 1

    # Envia ramal
    send_message "1234"
    sleep 1

    # Cancela ao invés de confirmar (para não criar ticket real)
    send_message "nao"

    echo "=== Teste Automatizado Concluído ==="
}

# Main
case "${1:-interactive}" in
    "health")
        check_health
        ;;
    "auto")
        check_health
        auto_test
        ;;
    "send")
        if [ -n "$2" ]; then
            send_message "$2"
        else
            echo "Uso: $0 send \"mensagem\""
        fi
        ;;
    "cancel")
        cancel_conversation
        ;;
    "interactive"|*)
        check_health
        interactive_mode
        ;;
esac
