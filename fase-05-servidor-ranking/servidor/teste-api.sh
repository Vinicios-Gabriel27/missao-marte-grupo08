#!/usr/bin/env bash
# Teste de ponta a ponta da API em execução (usado no CI e útil para a apresentação).
# Uso: ./teste-api.sh [http://localhost:8080]
set -euo pipefail
URL="${1:-http://localhost:8080}"
NOME="piloto$RANDOM"
echo "== saúde";        curl -fsS "$URL/actuator/health"; echo
echo "== cadastro";     curl -fsS -X POST "$URL/api/jogadores" -H 'Content-Type: application/json' -d "{\"nome\":\"$NOME\",\"senha\":\"senha123\"}"; echo
TOKEN=$(curl -fsS -X POST "$URL/api/sessoes" -H 'Content-Type: application/json' -d "{\"nome\":\"$NOME\",\"senha\":\"senha123\"}" | sed -E 's/.*"token":"([^"]+)".*/\1/')
echo "== login: token recebido (${#TOKEN} caracteres)"
echo "== partida";      curl -fsS -X POST "$URL/api/partidas" -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -d '{"pontuacao":1500,"dificuldade":"MEDIO","tempoSegundos":300,"nivel":12,"abates":80,"passageiros":5,"venceu":true}'; echo
echo "== sem token (espera 401)"; curl -s -o /dev/null -w "%{http_code}\n" -X POST "$URL/api/partidas" -H 'Content-Type: application/json' -d '{}'
echo "== ranking";      curl -fsS "$URL/api/ranking?limite=5"; echo
echo "OK"
