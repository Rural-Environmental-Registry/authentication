#!/bin/bash

echo "⏳ Aguardando o PostgreSQL iniciar..."
sleep 10  # Ajuste conforme necessário

echo "📁 Copiando DB_cardpg.sql para dentro do container..."
docker cp DB_cardpg.sql postgres-keycloak:/DB_cardpg.sql

echo "🔄 Restaurando o banco de dados..."
docker exec -i postgres-keycloak psql -U keycloak -d keycloak -f /DB_cardpg.sql

echo "✅ Restauração concluída com sucesso!"
