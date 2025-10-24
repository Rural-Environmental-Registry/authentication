package hammerConsult.cardpg.keycloak.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeycloakUserDTO {

    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String idNational;
    private boolean emailVerified;
    private long createdTimestamp;
    private boolean enabled = true;
    private boolean totp;
    private Map<String, List<String>> attributes;
    private List<String> disableableCredentialTypes;
    private List<String> requiredActions;
    private int notBefore;
    private UserAccessDTO access;
}
