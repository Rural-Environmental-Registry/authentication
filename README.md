# RER-DPG - Authentication

## Sobre o Módulo

O módulo **Authentication** é responsável pelo sistema de autenticação e autorização do RER-DPG, baseado em Keycloak com PostgreSQL. Inclui frontend administrativo e backend para gerenciamento de usuários e permissões, oferecendo suporte tanto para login tradicional quanto integração com GOV.BR.

**Principais características:**

- 🔐 Sistema de autenticação robusto com Keycloak
- 🌐 Interface administrativa moderna com Vue.js 3
- 🔄 Integração com GOV.BR para autenticação federada
- 🗄️ Persistência de dados com PostgreSQL
- 📊 Visualização centralizada de configurações do sistema
- 🛡️ Gerenciamento completo de usuários e permissões

---

## Pré-requisitos

- **Docker** versão 24+ ([instalação](https://docs.docker.com/engine/install/))
- **Docker Compose** versão 2.20 ou superior ([instalação](https://docs.docker.com/compose/install/linux/#install-using-the-repository))
- **Git** ([instalação](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git))

---

## Instalação e Execução

### Execução Integrada

Este módulo é executado automaticamente como parte do sistema RER-DPG principal. Para executar o sistema completo:

1. **No diretório principal do projeto:**
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
- Criar e iniciar os containers do PostgreSQL, Keycloak e Frontend
- Configurar a rede e volumes persistentes

---

## Configuração de Dados

### Importação Automática do Realm

O sistema possui importação automática configurada via Docker Compose. O arquivo `realm-export.json` na pasta `keycloak-import/` é importado automaticamente na inicialização.

### Opções de Restauração

Você pode escolher uma das abordagens abaixo:

- Restaurar o banco de dados completo via arquivo `.sql` (dump do banco)
- Importar o Realm via arquivo JSON (exportado do Keycloak)

### Opção 1 – Restauração via Dump do Banco

Com os containers rodando, execute:

```bash
bash restore.sh
```

**Importante:** Certifique-se de que o arquivo `DB_cardpg.sql` esteja presente no diretório.

### Opção 2 – Importação do Realm via JSON

#### Importação Automática

1. Copie o arquivo `realm-export.json` para a pasta `keycloak-import/`
2. Reinicie os containers:

```bash
docker-compose up --build
```

#### Exportação de Realm Existente

```bash
docker exec -it keycloak /bin/bash
/opt/keycloak/bin/kc.sh export --dir /opt/keycloak/data/import --realm car-dpg --users realm_file
exit
docker cp keycloak:/opt/keycloak/data/import ./keycloak-import
```

#### Importação Manual

1. Acesse: http://localhost:8080
2. Login: `admin` / `admin`
3. Create Realm → Browse → Selecione `realm-export.json`

---

## Acesso aos Serviços

Após a execução, os serviços estarão disponíveis:

- **Frontend Principal:** http://localhost/<BASE_URL>/<AUTHENTICATION_FRONTEND_CONTEXT_PATH>/login
- **Frontend Administrativo:** http://localhost/<BASE_URL>/<AUTHENTICATION_FRONTEND_CONTEXT_PATH>/admin-login
- **Keycloak Admin:** http://localhost/<BASE_URL>/<AUTHENTICATION_BASE_KEYCLOAK_BASE_URL>/admin

### Credenciais Administrativa Padrão

- **Usuário:** `admin-cardpg@gmail.com`
- **Senha:** `NovaSenhaForte123!`

---

## Funcionalidades da API

O backend oferece uma API REST completa para integração com Keycloak:

### Endpoints Principais

- **handleGovRedirect()** - Troca código OAuth por token de acesso
- **getAdminToken()** - Gera token administrativo
- **createUser()** - Cria novo usuário no Realm
- **buscarUsuarioPorUsername()** - Busca usuário por username
- **resetUserPassword()** - Redefine senha de usuário
- **atualizarUsuario()** - Atualiza dados de usuário

### Tipos de Autenticação

1. **Login Tradicional:** Usuário e senha diretamente na interface
2. **Login GOV.BR:** Integração com identidade federada via OAuth2

### Tecnologias

- Spring Boot
- RestTemplate para chamadas ao Keycloak
- DTOs customizados
- Tratamento de exceções específicas

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
├── backend/                  # Build da Aplicação backend (Java Spring Boot)
├── cardpg/                   # Source da Aplicação backend (Java Spring Boot)
├── frontend/                 # Interface administrativa (Vue.js 3)
├── keycloak-import/          # Realm exportado do Keycloak
├── providers/                # Provedores personalizados do Keycloak
├── themes/                   # Temas customizados para o Keycloak
├── DB_cardpg.sql             # Dump do banco PostgreSQL
├── docker-compose.yml        # Orquestração dos serviços
├── Dockerfile                # Imagem customizada do Keycloak
└── restore.sh                # Script de restauração do banco
```

---

## Notas Importantes

- O backend consome diretamente as APIs do Keycloak
- O dump do banco está disponível para consultas e evoluções futuras
- Sempre gere nova versão do dump para manter dados atualizados

## Problemas Comuns

- **Erro de conexão:** Verifique se o PostgreSQL foi inicializado antes do `restore.sh`
- **Arquivo de dump ausente:** Certifique-se de que `DB_cardpg.sql` está presente

---

## Licença

Este projeto é distribuído sob a [Licença MIT](https://opensource.org/license/mit).

---

## Contribuição

Contribuições são bem-vindas! Para contribuir:

1. Faça um fork do repositório
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

---

**Desenvolvido pela Superintendência de Inteligência Artificial e Inovação da Dataprev**
