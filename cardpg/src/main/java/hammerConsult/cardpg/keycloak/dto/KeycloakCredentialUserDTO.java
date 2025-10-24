package hammerConsult.cardpg.keycloak.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeycloakCredentialUserDTO {
    private String id;
	private String firstName;
    private String lastName;
	private String idNational;
    private String email;
    private String value;
    private String type ;
    private boolean temporary; 

}
