

const BASE_URL_KC = import.meta.env.VITE_KEYCLOAK_API_URL
const BASE_URL_BACK = import.meta.env.VITE_BACKEND_API_URL

export const API = {
  PROTOCOL: `${BASE_URL_KC}/realms/car-dpg/protocol`,
  ADMIN: `${BASE_URL_KC}/admin/realms/car-dpg`,
  ACCOUNT: `${BASE_URL_KC}/realms/car-dpg/account`,
  REGISTER: `${BASE_URL_BACK}/api/keycloak/createUser`,
  GENERAL: `${BASE_URL_BACK}/api/keycloak`,
  SEARCH: `${BASE_URL_BACK}/api/keycloak`,
  SEARCH_BY_USERNAME: `${BASE_URL_BACK}/api/keycloak/username`,
  CREDENTIAL: `${BASE_URL_BACK}/api/keycloak/credentials`,
  IDP : `${BASE_URL_BACK}/api/keycloak/federated-identity`
}

