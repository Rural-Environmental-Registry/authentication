package hammerConsult.cardpg.keycloak.dto;

import lombok.Data;

@Data
public class ProfileDTO {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String idNational;
}
