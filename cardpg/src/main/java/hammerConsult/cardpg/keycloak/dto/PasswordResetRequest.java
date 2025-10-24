package hammerConsult.cardpg.keycloak.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PasswordResetRequest {
    private String type;
    private boolean temporary = false;   
    private String value;

	public PasswordResetRequest() {}


}
