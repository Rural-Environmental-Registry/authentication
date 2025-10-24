package hammerConsult.cardpg.adapter;

import hammerConsult.cardpg.keycloak.adapter.CreateUserPasswordAdpter;
import hammerConsult.cardpg.keycloak.dto.KeycloakCredentialUserDTO;
import hammerConsult.cardpg.keycloak.dto.PasswordResetRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CreateUserPasswordAdpterTests {

    @Test
    void convert_devePreencherPasswordResetRequestCorretamente() {
        KeycloakCredentialUserDTO dto = new KeycloakCredentialUserDTO();
        dto.setValue("minhaSenhaSecreta");

        PasswordResetRequest result = CreateUserPasswordAdpter.convert(dto);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo("password");
        assertThat(result.getValue()).isEqualTo("minhaSenhaSecreta");
    }

    @Test
    void convert_deveAceitarValueNulo() {
        KeycloakCredentialUserDTO dto = new KeycloakCredentialUserDTO();
        dto.setValue(null);

        PasswordResetRequest result = CreateUserPasswordAdpter.convert(dto);

        assertThat(result.getType()).isEqualTo("password");
        //assertThat(result.getTemporary()).isFalse();
        assertThat(result.getValue()).isNull();
    }
}

