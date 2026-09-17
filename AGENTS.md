# Authentication

## Stack
- Keycloak 26 (identity provider)
- Spring Boot 3, Java 21, Gradle (cardpg backend)
- Vue.js 3, pnpm (frontend)
- PostgreSQL

## Comandos
```bash
# Build backend
cd cardpg && ./gradlew build
# Build frontend
cd frontend && pnpm install && pnpm build
# Test
cd cardpg && ./gradlew test
# Run local
docker-compose up
```

## Estrutura
```
cardpg/            # Spring Boot backend
frontend/          # Vue.js frontend
keycloak-import/   # realm config
themes/            # Keycloak themes
providers/         # Keycloak SPI providers
Dockerfile         # Keycloak container
```

## Convenções
- Java: PascalCase classes, camelCase métodos
- Vue: PascalCase components, camelCase props
- Commits: conventional commits (feat/fix/chore)
- Branches: develop → release/dev → release/qa → release/prd → main
