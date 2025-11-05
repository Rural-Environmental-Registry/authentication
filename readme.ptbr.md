# RER - Autenticação

[![Keycloak](https://img.shields.io/badge/Keycloak-23+-blue.svg)](https://www.keycloak.org/) [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-brightgreen.svg)](https://spring.io/projects/spring-boot) [![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/) [![Vue.js](https://img.shields.io/badge/Vue.js-3-green.svg)](https://vuejs.org/) [![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-blue.svg)](https://www.postgresql.org/) [![Docker](https://img.shields.io/badge/Docker-24+-blue.svg)](https://www.docker.com/)

## 📑 Índice

- [Sobre o Módulo](#sobre-o-módulo)
- [Instalação](#instalação)
- [Configuração de Dados](#configuração-de-dados)
- [Acesso aos Serviços](#acesso-aos-serviços)
- [Funcionalidades da API](#funcionalidades-da-api)
- [Tecnologias](#tecnologias)
- [Gerenciamento de Containers](#gerenciamento-de-containers)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Licença](#licença)
- [Contribuição](#contribuição)
- [Suporte](#suporte)

---

## 🎯 Sobre o Módulo

O **RER** (Rural Environmental Registry - Digital Public Good) é uma solução completa e moderna para o gerenciamento de cadastros ambientais rurais, desenvolvida como um bem público digital. O módulo **Authentication** faz parte do projeto RER como um submódulo.

É responsável pelo sistema de autenticação e autorização do RER, baseado em Keycloak com PostgreSQL. Inclui um frontend administrativo e backend para gerenciamento de usuários e permissões, suportando tanto login tradicional quanto integração com GOV.BR.

### Principais Características

- 🔐 Sistema de autenticação robusto com Keycloak
- 🌐 Interface administrativa moderna com Vue.js 3
- 🔄 Integração com autenticação SSO, como GOV.BR ou outros
- 🗄️ Persistência de dados com PostgreSQL
- 📊 Visualização centralizada de configurações do sistema
- 🛡️ Gerenciamento completo de usuários e permissões

---

## Instalação

### Pré-requisitos

- **Docker** versão 24+ ([instalação](https://docs.docker.com/engine/install/))
- **Docker Compose** versão 2.20+ ([instalação](https://docs.docker.com/compose/install/linux/#install-using-the-repository))
- **Git** ([instalação](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git))

### Execução Integrada

Este módulo é executado automaticamente como parte do sistema RER principal. Para executar o sistema completo:

```bash
./start.sh
```

### Execução Standalone

Para executar apenas o módulo Authentication:

```bash
docker-compose up --build
```

Este comando irá:

- Baixar as imagens necessárias
- Criar e iniciar os containers PostgreSQL, Keycloak e Frontend
- Configurar rede e volumes persistentes

---

## Configuração de Dados

### Importação Automática de Realm

O sistema possui importação automática configurada via Docker Compose. O arquivo `realm-export.json` na pasta `keycloak-import/` é importado automaticamente na inicialização.

### Opções de Restauração

Você pode escolher uma das seguintes abordagens:

- Restaurar o banco de dados completo via arquivo `.sql` (dump do banco)
- Importar o Realm via arquivo JSON (exportado do Keycloak)

#### Opção 1 – Restaurar via Dump do Banco de Dados

Com os containers em execução, execute:

```bash
bash restore.sh
```

**Importante**: Certifique-se de que o arquivo `DB_cardpg.sql` está presente no diretório.

#### Opção 2 – Importação de Realm via JSON

**Importação Automática**

- Copie o arquivo `realm-export.json` para a pasta `keycloak-import/`
- Reinicie os containers:

```bash
docker-compose up --build
```

**Exportar Realm Existente**

```bash
docker exec -it keycloak /bin/bash
/opt/keycloak/bin/kc.sh export --dir /opt/keycloak/data/import --realm car-dpg --users realm_file
exit
docker cp keycloak:/opt/keycloak/data/import ./keycloak-import
```

**Importação Manual**

- Acesse: `http://localhost:8080`
- Login: `admin / admin`
- Create Realm → Browse → Selecione `realm-export.json`

---

## Acesso aos Serviços

Após a execução, os serviços estarão disponíveis em:

- **Frontend Principal**: `http://localhost/<BASE_URL>/<AUTHENTICATION_FRONTEND_CONTEXT_PATH>/login`
- **Frontend Admin**: `http://localhost/<BASE_URL>/<AUTHENTICATION_FRONTEND_CONTEXT_PATH>/admin-login`
- **Keycloak Admin**: `http://localhost/<BASE_URL>/<AUTHENTICATION_BASE_KEYCLOAK_BASE_URL>/admin`

---

### Credenciais Padrão de Admin

- **Usuário**: `admin-cardpg@gmail.com`
- **Senha**: `NovaSenhaForte123!`

---

## Funcionalidades da API

O backend fornece uma API REST completa para integração com Keycloak:

### Principais Endpoints

- `handleGovRedirect()` – Troca código OAuth por token de acesso
- `getAdminToken()` – Gera token de administrador
- `createUser()` – Cria novo usuário no Realm
- `buscarUsuarioPorUsername()` – Busca usuário por username
- `resetUserPassword()` – Reseta senha do usuário
- `atualizarUsuario()` – Atualiza dados do usuário

### Tipos de Autenticação

- **Login Tradicional**: Usuário e senha diretamente na interface
- **Login com SSO**: Integração com sistemas de login como Gov.Br, Google ou outros sistemas SSO compatíveis com OIDC

---

## Tecnologias

- Spring Boot
- RestTemplate para chamadas Keycloak
- DTOs customizados
- Tratamento específico de exceções

---

## Gerenciamento de Containers

### Parar Serviços
```bash
docker-compose down
```

### Verificar Status
```bash
docker-compose ps
```

---

## Estrutura do Projeto

```
Authentication/
├── backend/                  # Build da aplicação backend (Java Spring Boot)
├── cardpg/                   # Código fonte do backend (Java Spring Boot)
├── frontend/                 # Interface administrativa (Vue.js 3)
├── keycloak-import/          # Realm exportado do Keycloak
├── providers/                # Providers customizados do Keycloak
├── themes/                   # Temas customizados do Keycloak
├── DB_cardpg.sql             # Dump do banco de dados PostgreSQL
├── docker-compose.yml        # Orquestração de serviços
├── Dockerfile                # Imagem customizada do Keycloak
└── restore.sh                # Script de restauração do banco
```

---

## Notas Importantes

- Backend consome diretamente APIs do Keycloak
- Dump do banco disponível para consultas e evolução futura
- Sempre gere uma nova versão do dump para manter dados atualizados

---

## Problemas Comuns

- **Erro de conexão**: Certifique-se de que o PostgreSQL está inicializado antes de executar `restore.sh`
- **Arquivo de dump ausente**: Certifique-se de que `DB_cardpg.sql` está presente

---

## Licença

Este projeto é distribuído sob a [GPL-3.0](https://github.com/Rural-Environmental-Registry/core/blob/main/LICENSE).

---

## Contribuição

Contribuições são bem-vindas! Para contribuir:

1. Faça um fork do repositório
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

Ao submeter um pull request ou patch, você afirma que é o autor do código e que concorda em licenciar sua contribuição sob os termos da Licença Pública Geral GNU v3.0 (ou posterior) deste projeto. Você também concorda em ceder os direitos autorais da sua contribuição ao Ministério da Gestão e Inovação em Serviços Públicos (MGI), titular deste projeto.

---

## Suporte

Para suporte técnico ou dúvidas sobre o projeto:

- **Documentação:** Consulte os READMEs individuais de cada submódulo
- **Issues:** Reporte problemas via sistema de issues do GitHub

---

## Atribuições

Para suporte técnico ou dúvidas sobre o projeto, por favor, registre um issue.

Copyright (C) 2024-2025 Ministério da Gestão e Inovação em Serviços Públicos (MGI), Governo do Brasil.

Este programa foi desenvolvido pela Dataprev como parte de um contrato com o Ministério da Gestão e Inovação em Serviços Públicos (MGI).
