package hammerConsult.cardpg.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import hammerConsult.cardpg.keycloak.dto.KeycloakCredentialUserDTO;
import hammerConsult.cardpg.keycloak.exception.ErrorGenericException;
import hammerConsult.cardpg.keycloak.utils.JwtDecodeUtils;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class JwtDecodeUtilsTests {
    private final ObjectMapper mapper = new ObjectMapper();


    @Test
    void decodeJwtClaims_deveRetornarMapaComClaimsValidos() throws Exception {
        String payloadJson = "{\"given_name\":\"Maria\",\"family_name\":\"Silva\",\"email\":\"maria@example.com\"}";
        String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));

        String jwt = "header." + encodedPayload + ".signature";

        Map<String, Object> claims = JwtDecodeUtils.decodeJwtClaims(jwt);

        assertThat(claims)
                .containsEntry("given_name", "Maria")
                .containsEntry("family_name", "Silva")
                .containsEntry("email", "maria@example.com");
    }

    @Test
    void decodeJwtClaims_deveLancarExcecaoSeJwtInvalido() {
        String jwtInvalido = "token-invalido";

        assertThatThrownBy(() -> JwtDecodeUtils.decodeJwtClaims(jwtInvalido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("JWT inválido");
    }

    @Test
    void decodeJwtClaims_deveLancarExcecaoSeJsonInvalido() {
        String payloadInvalido = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("###".getBytes(StandardCharsets.UTF_8));
        String jwt = "header." + payloadInvalido + ".signature";

        assertThatThrownBy(() -> JwtDecodeUtils.decodeJwtClaims(jwt))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha ao parsear claims");
    }
    @Test
    void extractDataToken_deveExtrairDadosCompletos() {
        Map<String, Object> claims = Map.of(
                "given_name", "João",
                "family_name", "Souza",
                "email", "joao@example.com",
                "id_national", "123456789"
        );

        KeycloakCredentialUserDTO dto = JwtDecodeUtils.extractDataToken(claims);

        assertThat(dto.getFirstName()).isEqualTo("João");
        assertThat(dto.getLastName()).isEqualTo("Souza");
        assertThat(dto.getEmail()).isEqualTo("joao@example.com");
        assertThat(dto.getIdNational()).isEqualTo("123456789");
    }

    @Test
    void extractDataToken_devePermitirValoresNulos() {
        Map<String, Object> claims = Map.of(); // vazio

        KeycloakCredentialUserDTO dto = JwtDecodeUtils.extractDataToken(claims);

        assertThat(dto.getFirstName()).isNull();
        assertThat(dto.getLastName()).isNull();
        assertThat(dto.getEmail()).isNull();
        assertThat(dto.getIdNational()).isNull();
    }

    @Test
    void extractDataToken_deveLancarErrorGenericExceptionSeFalhar() {
        Map<String, Object> claims = Map.of("given_name", new Object() {
            @Override
            public String toString() {
                throw new RuntimeException("boom");
            }
        });

        assertThatThrownBy(() -> JwtDecodeUtils.extractDataToken(claims))
                .isInstanceOf(ErrorGenericException.class)
                .hasMessageContaining("Erro ao extrair dados do token");
    }
}
