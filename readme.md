# RER - Authentication

[![Keycloak](https://img.shields.io/badge/Keycloak-23+-blue.svg)](https://www.keycloak.org/) [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-brightgreen.svg)](https://spring.io/projects/spring-boot) [![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/) [![Vue.js](https://img.shields.io/badge/Vue.js-3-green.svg)](https://vuejs.org/) [![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-blue.svg)](https://www.postgresql.org/) [![Docker](https://img.shields.io/badge/Docker-24+-blue.svg)](https://www.docker.com/)

## 📑 Table of Contents

- [About the Module](#about-the-module)
- [Installation](#installation)
- [Data Configuration](#data-configuration)
- [Service Access](#service-access)
- [API Features](#api-features)
- [Technologies](#technologies)
- [Container Management](#container-management)
- [Project Structure](#project-structure)
- [License](#license)
- [Contribution](#contribution)
- [Support](#support)

---

## 🎯 About the Module

The **RER** (Rural Environmental Registry) is a modern, comprehensive solution for managing rural environmental registrations, developed as a digital public good. This project provides a robust and scalable architecture for systems that register rural land properties with support for geospatial data. The **Authentication** module is part of RER project as a submodule from it.

It is responsible for the authentication and authorization system of RER, based on Keycloak with PostgreSQL. It includes an administrative frontend and backend for user and permission management, supporting both traditional login and integration with GOV.BR.

### Main Features

- 🔐 Robust authentication system with Keycloak  
- 🌐 Modern administrative interface with Vue.js 3  
- 🔄 Integration with SSO authentication, like GOV.BR or others
- 🗄️ Data persistence with PostgreSQL  
- 📊 Centralized system configuration visualization  
- 🛡️ Full user and permission management  

---

## Installation

### Prerequisites

- **Docker** version 24+ ([installation](https://docs.docker.com/engine/install/))
- **Docker Compose** version 2.20+ ([installation](https://docs.docker.com/compose/install/linux/#install-using-the-repository))
- **Git** ([installation](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git))

### Integrated Execution

This module runs automatically as part of the main RER system. To run the full system:

```bash
./start.sh
```

### Standalone Execution

To run only the Authentication module:

```bash
docker-compose up --build
```

This command will:

- Download required images  
- Create and start PostgreSQL, Keycloak, and Frontend containers  
- Configure network and persistent volumes  

---

## Data Configuration

### Automatic Realm Import

The system has automatic import configured via Docker Compose. The `realm-export.json` file in the `keycloak-import/` folder is automatically imported at startup.

### Restoration Options

You can choose one of the following approaches:

- Restore the full database via `.sql` file (database dump)  
- Import the Realm via JSON file (exported from Keycloak)  

#### Option 1 – Restore via Database Dump

With containers running, execute:

```bash
bash restore.sh
```

**Important**: Ensure the `DB_cardpg.sql` file is present in the directory.

#### Option 2 – Realm Import via JSON

**Automatic Import**

- Copy the `realm-export.json` file to the `keycloak-import/` folder  
- Restart the containers:

```bash
docker-compose up --build
```

**Export Existing Realm**

```bash
docker exec -it keycloak /bin/bash
/opt/keycloak/bin/kc.sh export --dir /opt/keycloak/data/import --realm car-dpg --users realm_file
exit
docker cp keycloak:/opt/keycloak/data/import ./keycloak-import
```

**Manual Import**

- Access: `http://localhost:8080`  
- Login: `admin / admin`  
- Create Realm → Browse → Select `realm-export.json`  

---

### Service Access

After execution, services will be available at:

- **Main Frontend**: `http://localhost/<BASE_URL>/<AUTHENTICATION_FRONTEND_CONTEXT_PATH>/login`  
- **Admin Frontend**: `http://localhost/<BASE_URL>/<AUTHENTICATION_FRONTEND_CONTEXT_PATH>/admin-login`  
- **Keycloak Admin**: `http://localhost/<BASE_URL>/<AUTHENTICATION_BASE_KEYCLOAK_BASE_URL>/admin`  

---

### Default Admin Credentials

- **User**: `admin-cardpg@gmail.com`  
- **Password**: `NovaSenhaForte123!`  

---

## API Features

The backend provides a complete REST API for integration with Keycloak:

### Main Endpoints

- `handleGovRedirect()` – Exchanges OAuth code for access token  
- `getAdminToken()` – Generates admin token  
- `createUser()` – Creates new user in Realm  
- `buscarUsuarioPorUsername()` – Searches user by username  
- `resetUserPassword()` – Resets user password  
- `atualizarUsuario()` – Updates user data  

### Authentication Types

- **Traditional Login**: Username and password directly in the interface  
- **Login with SSO:** Integration with login systems such as Gov.Br, Google, or other SSO systems compliant with OIDC

---

## Technologies

- Spring Boot  
- RestTemplate for Keycloak calls  
- Custom DTOs  
- Specific exception handling  

---

## Container Management

### Stop Services
```bash
docker-compose down
```

### Check Status
```bash
docker-compose ps
```

---

## Project Structure

```
Authentication/
├── backend/                  # Backend application build (Java Spring Boot)
├── cardpg/                   # Backend source code (Java Spring Boot)
├── frontend/                 # Admin interface (Vue.js 3)
├── keycloak-import/          # Exported Keycloak realm
├── providers/                # Custom Keycloak providers
├── themes/                   # Custom Keycloak themes
├── DB_cardpg.sql             # PostgreSQL database dump
├── docker-compose.yml        # Service orchestration
├── Dockerfile                # Custom Keycloak image
└── restore.sh                # Database restore script
```

---

## Important Notes

- Backend directly consumes Keycloak APIs  
- Database dump is available for queries and future evolution  
- Always generate a new dump version to keep data updated  

---

## Common Issues

- **Connection error**: Ensure PostgreSQL is initialized before running `restore.sh`  
- **Missing dump file**: Ensure `DB_cardpg.sql` is present  

---

## License

This project is distributed under the [GPL-3.0](https://github.com/Rural-Environmental-Registry/core/blob/main/LICENSE).

---

## Contribution

Contributions are welcome! To contribute:

1. Fork the repository
2. Create a branch for your feature (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

By submitting a pull request or patch, you affirm that you are the author of the code and that you agree to license your contribution under the terms of the GNU General Public License v3.0 (or later) for this project. You also agree to assign the copyright of your contribution to the Ministry of Management and Innovation in Public Services (MGI), the owner of this project.

---

## Support

For technical support or project-related questions:

- **Documentation:** Check the individual READMEs for each submodule
- **Issues:** Report problems via the GitHub issue tracker

---

## Responsibilities

For technical support or questions about the project, please, fill a issue.

Copyright (C) 2024-2025 Ministry of Management and Innovation in Public Services (MGI), Government of Brazil.
 
This program was developed by Dataprev as part of a contract with the Ministry of Management and Innovation in Public Services (MGI).