export const TABLE_DATA = [
  {
    fieldName: "frontend_url (CORS)",
    value: "https://inovacao.dataprev.gov.br/",
    source: "docker-compose.yaml",
    component: "backend"
  },
  {
    fieldName: "backend_version",
    value: "0.0.1-SNAPSHOT",
    source: "build.gradle",
    component: "backend"
  },
  {
    fieldName: "spring.application.name",
    value: "Registration",
    source: "application.properties",
    component: "backend"
  },
  {
    fieldName: "server.servlet.context-path",
    value: "/cardpgbackend",
    source: "src/main/resources/application.properties",
    component: "backend"
  },
  {
    fieldName: "fields.person",
    value: "[uuid, identifier, date_of_birth, mothers_name]",
    source: "database",
    component: "backend"
  },
  {
    fieldName: "fields.property",
    value: "[uuid,property_id, geometry, propertyname, state_district, municipality, zipcode, locationzone]",
    source: "database",
    component: "backend"
  },
  {
    fieldName: "fields.property_document",
    value: "[uuid, property_id, registered_property_name, área, document_type, has_legal_reserve]",
    source: "database",
    component: "backend"
  },
  {
    fieldName: "fields.sub_area",
    value: "[uuid, geometry, property_id, área, areatype]",
    source: "database",
    component: "backend"
  },
  {
    fieldName: "fields.ship",
    value: "[uuid, type: {OWNER, REGISTRAR, REPRESENTATIVE}, property_id]",
    source: "database",
    component: "backend"
  },
  {
    fieldName: "customAttributes.person.owner",
    value: "[landholderType: {natural_person || legal_entity}]",
    source: "database",
    component: "backend"
  },
  {
    fieldName: "customAttributes.property",
    value: "[mailing_address_recipient_name, mailing_address_street, mailing_address_number, mailing_address_neighborhood, mailing_address_zip_code, mailing_address_state, mailing_address_city, property_access_description, mailing_address_additional_info, mailing_address_email, mailing_address_telephone]",
    source: "database",
    component: "backend"
  },
  {
    fieldName: "customAttributes.property_document",
    value: "[holdingType: {property,landholding}, deed, documentDate, book, page, state_of_notary_office, city_of_notary_office, national_rural_land_system_code, property_certification, national_rural_land_registration_number]",
    source: "database",
    component: "backend"
  },
  {
    fieldName: "email.smtp.server",
    value: "A definir",
    source: "A definir",
    component: "backend"
  },
  {
    fieldName: "email.smtp.port",
    value: "A definir",
    source: "A definir",
    component: "backend"
  },
  {
    fieldName: "email.smtp.auth",
    value: "A definir",
    source: "A definir",
    component: "backend"
  },
  {
    fieldName: "email.smtp.starttls.enable",
    value: "A definir",
    source: "A definir",
    component: "backend"
  },
  {
    fieldName: "email.smtp.user",
    value: "A definir",
    source: "A definir",
    component: "backend"
  },
  {
    fieldName: "email.smtp.password",
    value: "A definir",
    source: "A definir",
    component: "backend"
  },
  {
    fieldName: "Campos_de_configuração_módulo_autenticação",
    value: "A definir",
    source: "keycloak",
    component: "backend"
  },
  {
    fieldName: "frontend_project_name",
    value: "vuejs-car-dpg-frontend",
    source: "package.json",
    component: "frontend"
  },
  {
    fieldName: "frontend_version",
    value: "0.0.0",
    source: "package.json",
    component: "frontend"
  },
  {
    fieldName: "backend_url",
    value: "https://inovacao.dataprev.gov.br/cardpgbackend/v1",
    source: ".env",
    component: "frontend"
  },
  {
    fieldName: "vite_base_url",
    value: "/rechml",
    source: ".env",
    component: "frontend"
  },
  {
    fieldName: "mapa_center (lat, lon)",
    value: "[-15.235, -51.9253]",
    source: "src/assets/map/constsMap.ts -> MAP_OPTIONS.map.config.center",
    component: "frontend"
  },
  {
    fieldName: "mapa_zoon_inicial",
    value: "4",
    source: "src/assets/map/constsMap.ts -> MAP_OPTIONS.map.config.zoom",
    component: "frontend"
  },
  {
    fieldName: "mapa_camadas_base[]",
    value: "objeto com as configurações",
    source: "src/assets/map/layers.json -> mapLayers",
    component: "frontend"
  },
  {
    fieldName: "mapa_camadas_customizadas[]",
    value: "objeto com as configurações",
    source: "src/assets/map/layers.json -> customLayers",
    component: "frontend"
  },
  {
    fieldName: "linguagem_padrão",
    value: "en-us",
    source: "src/config/languages.json.default",
    component: "frontend"
  },
  {
    fieldName: "configurações_motor_de cálculo",
    value: "A definir",
    source: "database",
    component: "motor de cálculo"
  }
];
