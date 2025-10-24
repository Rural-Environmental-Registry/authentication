# -------------------------------------------------------------
# Especificar imagem base personalizada do Keycloak
# -------------------------------------------------------------
    FROM ruancruz0325/keycloak-car-dpg:1.04

    # -------------------------------------------------------------
    # Copiar temas personalizados e extensões (providers) para Keycloak
    # -------------------------------------------------------------
    COPY themes /opt/keycloak/themes
    COPY providers /opt/keycloak/providers
    
    # -------------------------------------------------------------
    # Desativar cache de temas e templates para facilitar desenvolvimento
    # -------------------------------------------------------------
    ENV KC_SPI_THEME_CACHE_THEMES=false
    ENV KC_SPI_THEME_CACHE_TEMPLATES=false
    
    # -------------------------------------------------------------
    # Configurar Keycloak com banco de dados PostgreSQL
    # -------------------------------------------------------------
    ENV KC_DB=postgres
    
    # -------------------------------------------------------------
    # Habilitar funcionalidades experimentais
    # -------------------------------------------------------------
    ENV KC_FEATURES=preview
    
    # -------------------------------------------------------------
    # Definir caminho relativo (context path) da aplicação
    # -------------------------------------------------------------
    ENV KC_HTTP_RELATIVE_PATH=/rechml/keycloak
    
    # -------------------------------------------------------------
    # Configurar proxy para aceitar cabeçalhos encaminhados
    # -------------------------------------------------------------
    ENV KC_PROXY=passthrough
    ENV PROXY_ADDRESS_FORWARDING=true
    ENV KC_PROXY_HEADERS=xforwarded
    ENV KC_PROXY=edge
    
    # -------------------------------------------------------------
    # Habilitar HTTP (útil para ambiente de desenvolvimento)
    # -------------------------------------------------------------
    ENV KC_HTTP_ENABLED=true
    
    # -------------------------------------------------------------
    # Construir o Keycloak com o context path e features ativadas
    # -------------------------------------------------------------
    RUN /opt/keycloak/bin/kc.sh build --features=scripts
    