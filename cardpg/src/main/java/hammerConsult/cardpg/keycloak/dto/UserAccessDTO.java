package hammerConsult.cardpg.keycloak.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserAccessDTO {
	private boolean manageGroupMembership;
	private boolean view;
	private boolean mapRoles;
	private boolean impersonate;
	private boolean manage;

}
