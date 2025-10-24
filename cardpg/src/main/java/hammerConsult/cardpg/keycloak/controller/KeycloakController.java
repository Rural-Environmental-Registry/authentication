package hammerConsult.cardpg.keycloak.controller;

import java.util.List;
import java.util.Map;

import hammerConsult.cardpg.keycloak.dto.ProfileDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import hammerConsult.cardpg.keycloak.dto.KeycloakCredentialUserDTO;
import hammerConsult.cardpg.keycloak.dto.KeycloakUserDTO;
import hammerConsult.cardpg.keycloak.service.KeycloakService;

@RestController
@RequestMapping("/api/keycloak")
public class KeycloakController {

    private final KeycloakService keycloakService;

    public KeycloakController(KeycloakService keycloakService) {
        this.keycloakService = keycloakService;
    }

    // GET /api/keycloak/token
    @GetMapping("/token")
    public ResponseEntity<String> getAdminToken() {
        String token = keycloakService.getAdminToken();
        return ResponseEntity.ok(token);
    }
    @PostMapping("/auth/callback")
    public ResponseEntity<Map<String, Object>> handleGovRedirect(@RequestParam("code") String code){
        return keycloakService.handleGovRedirect(code);
    }
    @PostMapping("/usuario")
    public ResponseEntity<String> createUser(@RequestBody KeycloakCredentialUserDTO userDTO) {
        keycloakService.createUser(userDTO);
    	return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    // GET /api/keycloak/token
    @GetMapping("/username/{username}")
    public List<KeycloakUserDTO> getUser(@PathVariable String username) {
    	return keycloakService.buscarUsuarioPorUsername(username);
    }

    @GetMapping("/{id}")
    public ProfileDTO getUserById(@PathVariable String id) {
        return keycloakService.buscarDadosUsuario(id);
    }

    @PutMapping("/resetPassword")
    public ResponseEntity<String> resetPassword(@RequestBody KeycloakCredentialUserDTO userDTO) {
    	return keycloakService.resetUserPassword(userDTO.getId(),userDTO.getValue());
    }
    @PostMapping("/createUser")
    public ResponseEntity<String> createUserComplete(@RequestBody KeycloakCredentialUserDTO userDTO) {
    	return keycloakService.createUserComplete(userDTO);
    }
    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> handlePreflight() {
        return ResponseEntity.ok().build();
    }

    @PutMapping("/updatePerfil")
    public ResponseEntity<String> updatePerfil(@RequestBody KeycloakUserDTO userDTO) {
    	return keycloakService.atualizarUsuario(userDTO);
    }

    @GetMapping("/federated-identity/{username}")
    public ResponseEntity<Boolean> isIdp(@RequestBody @PathVariable String username) {
        return keycloakService.checarIdp(username);
    }

    @GetMapping("/credentials/{username}")
    public ResponseEntity<Boolean> haveCredential(@PathVariable String username) {
        return keycloakService.temSenha(username);
    }
}
