# RER-DPG - Authentication Frontend

## Sobre o Módulo

O **Authentication Frontend** é a interface administrativa do módulo de autenticação do RER-DPG, desenvolvida em Vue.js 3 com Vite. Fornece uma interface moderna e intuitiva para gerenciamento de usuários, visualização de configurações do sistema e administração do Keycloak.

**Principais características:**

- 🌐 Interface administrativa moderna com Vue.js 3
- ⚡ Build otimizado com Vite e TypeScript
- 🔐 Gerenciamento completo de usuários e permissões
- 📊 Visualização centralizada de configurações do sistema
- 🎨 Design responsivo com Tailwind CSS
- 🧪 Testes unitários e E2E integrados
- 🔄 Integração com APIs do Keycloak

---

## Pré-requisitos

- **Node.js** versão 18+ ([instalação](https://nodejs.org/))
- **pnpm** ([instalação](https://pnpm.io/installation))
- **Vue.js 3** ([documentação](https://vuejs.org/))
- **TypeScript** ([documentação](https://www.typescriptlang.org/))

---

## Configuração do Ambiente de Desenvolvimento

### IDE Recomendado

- **VSCode** + **Volar** (desabilite o Vetur)
- **TypeScript Vue Plugin (Volar)** para suporte completo ao Vue 3

### Instalação de Dependências

```bash
pnpm install
```

---

## Scripts Disponíveis

### Desenvolvimento

```bash
pnpm dev
```

Inicia o servidor de desenvolvimento com hot-reload.

### Build de Produção

```bash
pnpm build
```

Compila e minifica para produção com verificação de tipos.

### Testes

#### Testes Unitários

```bash
pnpm test:unit
```

Executa testes unitários com [Vitest](https://vitest.dev/).

#### Testes E2E

```bash
# Desenvolvimento (mais rápido)
pnpm test:e2e:dev

# Produção (recomendado para CI)
pnpm build
pnpm test:e2e
```

Executa testes end-to-end com [Cypress](https://www.cypress.io/).

### Qualidade de Código

```bash
pnpm lint
```

Executa linting com [ESLint](https://eslint.org/).

---

## Funcionalidades

### Visualização de Configurações

A principal funcionalidade é a **página de visualização de configurações do sistema** (`AdminSettings.vue`), que:

- Coleta configurações de todos os submódulos do RER-DPG
- Exibe informações em tabela pesquisável e organizável
- Identifica origem de cada configuração (arquivo, componente)
- Permite navegação e filtragem avançada

### Gerenciamento de Usuários

- Interface para criação e edição de usuários
- Integração com APIs do Keycloak
- Gerenciamento de permissões e roles
- Visualização de dados de autenticação

### Tecnologias

- **Vue.js 3** (Composition API)
- **Vite** (Build tool)
- **TypeScript** (Tipagem estática)
- **Tailwind CSS** (Estilização)
- **Vitest** (Testes unitários)
- **Cypress** (Testes E2E)
- **ESLint** (Linting)
- **Prettier** (Formatação)

---

## Estrutura do Projeto

```
Authentication/frontend/
├── mocks/                      # Dados mock para desenvolvimento
├── public/                     # Assets públicos
├── src/
│   ├── components/             # Componentes Vue reutilizáveis
│   ├── helpers/                # Funções auxiliares
│   │   └── table.ts           # Parser de configurações
│   ├── views/                  # Páginas/Views
│   │   └── AdminSettings.vue  # Página principal de configurações
│   ├── App.vue                # Componente raiz
│   └── main.ts                # Ponto de entrada
├── .env                        # Variáveis de ambiente
├── .env.production            # Variáveis de produção
├── components.json            # Configuração de componentes
├── Dockerfile                 # Imagem Docker
├── nginx.conf                 # Configuração Nginx
├── package.json               # Dependências e scripts
├── tailwind.config.js         # Configuração Tailwind
├── tsconfig.json              # Configuração TypeScript
├── vite.config.ts             # Configuração Vite
└── vitest.config.ts           # Configuração Vitest
```

---

## Configuração

### Variáveis de Ambiente

Principais variáveis no arquivo `.env`:

- `VITE_API_URL` - URL da API backend
- `VITE_KEYCLOAK_URL` - URL do Keycloak
- `VITE_APP_TITLE` - Título da aplicação

### Configuração do Vite

Consulte [Vite Configuration Reference](https://vite.dev/config/) para customizações avançadas.

---

## Integração com Sistema Principal

Este frontend integra-se com o sistema RER-DPG através de:

1. **Parser de Configurações** (`helpers/table.ts`): Interpreta dados de configuração dos diferentes submódulos
2. **APIs do Core-Backend**: Consome endpoint `/v1/admin/app-info` para configurações do servidor
3. **APIs do Core-Frontend**: Acessa `config.json` gerado pelo script `generate-config.sh`
4. **Keycloak Integration**: Comunicação direta com APIs do Keycloak

---

## Acesso à Aplicação

Quando executado como parte do sistema RER-DPG:

- **URL:** http://localhost/<BASE_URL>/<AUTHENTICATION_FRONTEND_CONTEXT_PATH>
- **Credenciais padrão:**
  - **Usuário:** `admin-cardpg@gmail.com`
  - **Senha:** `NovaSenhaForte123!`

---

## Desenvolvimento

### Suporte a TypeScript

O projeto utiliza `vue-tsc` para verificação de tipos em arquivos `.vue`. O Volar é necessário no editor para suporte completo ao TypeScript em componentes Vue.

### Hot Module Replacement

O Vite fornece HMR otimizado para desenvolvimento rápido com Vue 3.

### Testes

- **Unitários:** Focam em lógica de componentes e helpers
- **E2E:** Testam fluxos completos da interface administrativa

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
