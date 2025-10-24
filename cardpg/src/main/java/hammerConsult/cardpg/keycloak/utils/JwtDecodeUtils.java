package hammerConsult.cardpg.keycloak.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hammerConsult.cardpg.keycloak.dto.KeycloakCredentialUserDTO;
import hammerConsult.cardpg.keycloak.exception.ErrorGenericException;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@UtilityClass
public class JwtDecodeUtils {

    public Map<String, Object> decodeJwtClaims(String jwt) {
        String[] parts = jwt.split("\\.");
        if (parts.length < 2) throw new IllegalArgumentException("JWT inválido");
        String json = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        try {
            return new ObjectMapper().readValue(json, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new RuntimeException("Falha ao parsear claims", e);
        }
    }


    public static KeycloakCredentialUserDTO extractDataToken(Map<String, Object> claims) {
        try {
            String firstName = Optional.ofNullable(claims.get("given_name")).map(Object::toString).orElse(null);
            String lastName = Optional.ofNullable(claims.get("family_name")).map(Object::toString).orElse(null);
            String email = Optional.ofNullable(claims.get("email")).map(Object::toString).orElse(null);
            String idNational = Optional.ofNullable(claims.get("id_national")).map(Object::toString).orElse(null);

            KeycloakCredentialUserDTO credentialUserDTO = new KeycloakCredentialUserDTO();
            credentialUserDTO.setFirstName(firstName);
            credentialUserDTO.setLastName(lastName);
            credentialUserDTO.setEmail(email);
            credentialUserDTO.setIdNational(idNational);
            return credentialUserDTO;
        }catch (Exception e){
            throw new ErrorGenericException("Erro ao extrair dados do token: " + e.getMessage());
        }
    }
}
