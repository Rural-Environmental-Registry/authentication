package hammerConsult.cardpg.keycloak.adapter;

import hammerConsult.cardpg.keycloak.dto.KeycloakCredentialUserDTO;
import hammerConsult.cardpg.keycloak.dto.PasswordResetRequest;

public class CreateUserPasswordAdpter {
	
	private final static String PASSWORD_TYPE = "password";

	public static PasswordResetRequest convert(KeycloakCredentialUserDTO request) {
		PasswordResetRequest passwordRequest = new PasswordResetRequest();
		
		passwordRequest.setType(PASSWORD_TYPE);
		passwordRequest.setTemporary(Boolean.FALSE);
		passwordRequest.setValue(request.getValue());
		return passwordRequest;
		
	}
}
