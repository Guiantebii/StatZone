#!/bin/bash
# Uso: ./seed.sh [DB_USERNAME] [DB_PASSWORD]

DB_USERNAME="${1:-myuser}"
DB_PASSWORD="${2:-postgres}"

BACKEND_DIR="$(dirname "$0")/../statezone-backend"

echo "=== DataSeeder ==="
echo "Isso vai DELETAR todos os dados e recriar!"
echo "Enter = continuar | Ctrl+C = cancelar"
read -r

cd "$BACKEND_DIR"

DB_USERNAME="$DB_USERNAME" \
DB_PASSWORD="$DB_PASSWORD" \
JWT_SECRET="qualquer_chave_aqui_123" \
JWT_EXPIRATION_MS="86400000" \
API_FOOTBALL_KEY="fake_key" \
  mvn spring-boot:run -Dspring-boot.run.profiles=seed -q
