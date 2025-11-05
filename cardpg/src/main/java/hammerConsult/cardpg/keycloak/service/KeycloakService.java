package hammerConsult.cardpg.keycloak.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import hammerConsult.cardpg.keycloak.adapter.CreateUserPasswordAdpter;
import hammerConsult.cardpg.keycloak.dto.KeycloakCredentialUserDTO;
import hammerConsult.cardpg.keycloak.dto.KeycloakUserDTO;
import hammerConsult.cardpg.keycloak.dto.PasswordResetRequest;
import hammerConsult.cardpg.keycloak.dto.ProfileDTO;
import hammerConsult.cardpg.keycloak.exception.ErrorGenericException;
import hammerConsult.cardpg.keycloak.exception.InvalidPasswordException;
import hammerConsult.cardpg.keycloak.exception.UserNotFoundException;
import hammerConsult.cardpg.keycloak.utils.JwtDecodeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.*;

@Service
@RequiredArgsConstructor
public class KeycloakService {

    @Value("${keycloak.url}")
    private String keycloakUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${keycloak.redirect-uri}")
    private String redirectUri;

    @Value("${backend.base-url}")
    private String baseUrl;

    private final static String ADMIN = "admin";

    private final static String REALM_ADMIN = "realm-admin";

    public RestTemplate restTemplate = new RestTemplate();

    public ResponseEntity<Map<String, Object>> handleGovRedirect(@RequestParam("code") String code) {
        String tokenUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);

        try {
            ResponseEntity<Map<String, Object>> resp = restTemplate.exchange(
                    tokenUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    new ParameterizedTypeReference<>() {
                    }
            );

            Map<String, Object> tokenResponse = resp.getBody();
            if (tokenResponse == null) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body(Map.of("error", "Resposta vazia do servidor de autorização"));
            }

            String accessToken = (String) tokenResponse.get("access_token");
            if (accessToken != null && !accessToken.isBlank()) {
                Map<String, Object> claims = JwtDecodeUtils.decodeJwtClaims(accessToken);
                KeycloakCredentialUserDTO credentialUserDTO = JwtDecodeUtils.extractDataToken(claims);
                String sub = Optional.ofNullable(claims.get("sub")).map(Object::toString).orElse(null);
                saveUser(sub, credentialUserDTO);
            }
            return ResponseEntity.ok(resp.getBody());
        } catch (HttpClientErrorException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Erro ao trocar o code pelo token " + code);
            error.put("status", e.getMessage() + " - " + e.getStatusCode().value());
            return ResponseEntity.status(e.getStatusCode().value()).body(error);
        }
    }

    public String getAdminToken() {
        String tokenUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            return (String) response.getBody().get("access_token");
        } catch (HttpClientErrorException e) {
            throw new ErrorGenericException("Erro ao obter token com client_credentials: " + e.getMessage());
        }
    }

    public String createUser(@RequestBody KeycloakCredentialUserDTO userDTO) {
        String accessToken = getAdminToken();
        String createUserUrl = keycloakUrl + "/admin/realms/" + realm + "/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        Map<String, Object> user = new HashMap<>();
        user.put("username", userDTO.getEmail());
        user.put("enabled", Boolean.TRUE);
        user.put("firstName", userDTO.getFirstName());
        user.put("lastName", userDTO.getLastName());
        user.put("email", userDTO.getEmail());
        user.put("emailVerified", Boolean.TRUE);

        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("idNational", Collections.singletonList(userDTO.getIdNational()));
        user.put("attributes", attributes);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(user, headers);

        try {
            ResponseEntity<Void> resp = restTemplate.exchange(createUserUrl, HttpMethod.POST, request, Void.class);

            if (resp.getStatusCode() != HttpStatus.CREATED) {
                throw new ErrorGenericException("Falha ao criar usuário. Status: " + resp.getStatusCode());
            }

            URI location = resp.getHeaders().getLocation();
            if (location == null) {
                throw new ErrorGenericException("Usuário criado, mas o Location não veio no header.");
            }
            String userId = location.getPath().substring(location.getPath().lastIndexOf('/') + 1);

            if (userDTO.getValue() != null && !userDTO.getValue().isBlank()) {
                resetUserPassword(userId, userDTO.getValue());
            }
            return userId;
        } catch (HttpClientErrorException.Conflict e) {
            throw new InvalidPasswordException("Usuário já existe com esse username/email.");
        } catch (HttpStatusCodeException e) {
            throw new ErrorGenericException("Erro ao criar usuário: HTTP " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new ErrorGenericException("Erro ao criar usuário: " + e.getMessage());
        }
    }

    public void validateIfExist(String email, String idNational) {
        if (!buscarUsuarioPorUsername(email).isEmpty()) {
            throw new InvalidPasswordException(
                    "Usuário com email " + email + " já existe."
            );
        }

        if (!buscarUsuarioPorIdNational(idNational).isEmpty()) {
            throw new InvalidPasswordException(
                    "Usuário com idNational " + idNational + " já existe."
            );
        }
    }


    public List<KeycloakUserDTO> buscarUsuarioPorUsername(String usernameBody) {
        String createUserUrl = keycloakUrl + "/admin/realms/" + realm + "/users?username=" + usernameBody;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAdminToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<List<KeycloakUserDTO>> response = restTemplate.exchange(createUserUrl, HttpMethod.GET,
                    entity, new ParameterizedTypeReference<>() {
                    });
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new UserNotFoundException("Erro Inesperado ao buscar usuario");
        }
    }

    public List<KeycloakUserDTO> buscarUsuarioPorIdNational(String idNational) {
        String url = keycloakUrl + "/admin/realms/" + realm + "/users?q=idNational:" + idNational;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAdminToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<List<KeycloakUserDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {
                    }
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new UserNotFoundException("Erro inesperado ao buscar usuário por idNational");
        }
    }

    public KeycloakUserDTO buscarUsuarioPorId(String userId) {
        String url = keycloakUrl + "/admin/realms/" + realm + "/users/" + userId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAdminToken()); // Assumindo que você já tem esse método
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<KeycloakUserDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    KeycloakUserDTO.class
            );

            return response.getBody();

        } catch (HttpClientErrorException.NotFound e) {
            throw new UserNotFoundException("Usuário com ID: " + userId + " não encontrado.");
        } catch (HttpClientErrorException e) {
            throw new UserNotFoundException("Erro ao buscar usuário com ID: " + userId);
        }
    }

    public ResponseEntity<String> resetUserPassword(String userId, String newPassword) {
        String resetPasswordUrl = keycloakUrl + "/admin/realms/" + realm + "/users/" + userId + "/reset-password";

        String username = buscarUsuarioPorId(userId).getUsername();

        if (isAdmin(getAdminToken(), username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Por política de segurança, não é permitido alterar o usuário Admin");
        }

        // Criar corpo da requisição
        KeycloakCredentialUserDTO request = new KeycloakCredentialUserDTO();
        request.setValue(newPassword);

        // Cabeçalhos
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getAdminToken());

        HttpEntity<PasswordResetRequest> entity = new HttpEntity<>(CreateUserPasswordAdpter.convert(request), headers);

        try {
            restTemplate.exchange(resetPasswordUrl, HttpMethod.PUT, entity, Void.class);
            return ResponseEntity.ok("Senha alterada com sucesso.");

        } catch (HttpClientErrorException.BadRequest e) {
            throw new InvalidPasswordException(
                    "A senha deve conter pelo menos oito caracteres, incluindo uma letra maiúscula, uma letra minúscula, um número e um caractere especial, como por exemplo, Segura#2025");
        }

    }

    public ResponseEntity<String> createUserComplete(KeycloakCredentialUserDTO user) {
        validateIfExist(user.getEmail(), user.getIdNational());
        String userId = createUser(user);
        if (userId != null && !userId.isBlank()) {
            saveUser(userId, user);
        } else {
            throw new ErrorGenericException("Erro ao criar usuário");
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    public void saveUser(String id, KeycloakCredentialUserDTO userKc) {

        String createUserUrl = baseUrl + "/v1/users";

        Map<String, Object> user = new HashMap<>();
        user.put("idKeycloak", id);
        user.put("firstName", userKc.getFirstName());
        user.put("lastName", userKc.getLastName());
        user.put("idNational", userKc.getIdNational());
        user.put("email", userKc.getEmail());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(user);

        try {
            restTemplate.postForEntity(createUserUrl, request, String.class);
        } catch (HttpClientErrorException e) {
            e.printStackTrace();
            throw new InvalidPasswordException("Erro ao salvar usuario no Core");
        }

    }

    public ResponseEntity<String> atualizarUsuario(KeycloakUserDTO dadosAtualizados) {
        KeycloakUserDTO keycloakUserDTO = buscarUsuarioPorId(dadosAtualizados.getId());
        if (keycloakUserDTO == null) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }

        validateExisteForUpdate(dadosAtualizados, keycloakUserDTO.getId());

        String url = keycloakUrl + "/admin/realms/" + realm + "/users/" + keycloakUserDTO.getId();

        if (isAdmin(getAdminToken(), dadosAtualizados.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Por política de segurança, não é permitido alterar o usuário Admin");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAdminToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        dadosAtualizados.setId(keycloakUserDTO.getId());

        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("idNational", Collections.singletonList(dadosAtualizados.getIdNational()));
        dadosAtualizados.setAttributes(attributes);


        HttpEntity<KeycloakUserDTO> request = new HttpEntity<>(dadosAtualizados, headers);

        restTemplate.exchange(url, HttpMethod.PUT, request, Void.class);

        atualizarUsuarioCore(dadosAtualizados, keycloakUserDTO.getId());

        return ResponseEntity.ok("Usuario atualizado com Sucesso.");

    }

    private void validateExisteForUpdate(KeycloakUserDTO dadosAtualizados, String id) {
        buscarUsuarioPorUsername(dadosAtualizados.getEmail()).stream()
                .filter(u -> !u.getId().equals(id))
                .filter(u -> u.getEmail() != null && u.getEmail().equalsIgnoreCase(dadosAtualizados.getEmail()))
                .findFirst()
                .ifPresent(u -> {
                    throw new InvalidPasswordException("Usuário com email " + u.getEmail() + " já existe.");
                });

        buscarUsuarioPorIdNational(dadosAtualizados.getIdNational()).stream()
                .filter(u -> !u.getId().equals(id))
                .map(u -> Optional.ofNullable(u.getAttributes().get("idNational"))
                        .filter(list -> !list.isEmpty())
                        .map(list -> list.get(0))
                        .orElse(null))
                .filter(Objects::nonNull)
                .filter(idNat -> idNat.equals(dadosAtualizados.getIdNational()))
                .findFirst()
                .ifPresent(idNat -> {
                    throw new InvalidPasswordException("Usuário com idNational " + idNat + " já existe.");
                });
    }

    public void atualizarUsuarioCore(KeycloakUserDTO dados, String id) {
        String url = baseUrl + "/v1/users/keycloak/" + id;

        Map<String, Object> user = new HashMap<>();
        user.put("firstName", dados.getFirstName());
        user.put("lastName", dados.getLastName());
        user.put("email", dados.getEmail());
        user.put("idNational", dados.getIdNational());

        try {
            restTemplate.put(url, user);
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Erro ao salvar usuário no Core: " + e.getStatusCode(), e);
        }
    }

    public boolean isAdmin(String token, String usernamePassado) {
        DecodedJWT jwt = JWT.decode(token);

        // Extrai o username do token
        String username = jwt.getClaim("preferred_username").asString();

        // Extrai as roles de realm_access.roles
        List<String> realmRoles = (List<String>) jwt.getClaim("realm_access").asMap().get("roles");

        // Verifica se o username é "admin", se contém o nome admin no parâmetro ou se tem o papel realm-admin
        return ADMIN.equalsIgnoreCase(username)
                || (realmRoles != null && realmRoles.contains(REALM_ADMIN))
                || (usernamePassado != null && usernamePassado.toLowerCase().contains(ADMIN));
    }

    public ProfileDTO buscarDadosUsuario(String id) {
        KeycloakUserDTO keycloakUserDTO = buscarUsuarioPorId(id);
        String idNational = null;
        if (keycloakUserDTO.getAttributes() != null) {
            idNational = Optional.ofNullable(keycloakUserDTO.getAttributes().get("idNational"))
                    .filter(list -> !list.isEmpty())
                    .map(list -> list.get(0))
                    .orElse(null);

        }
        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setId(keycloakUserDTO.getId());
        profileDTO.setFirstName(keycloakUserDTO.getFirstName());
        profileDTO.setLastName(keycloakUserDTO.getLastName());
        profileDTO.setEmail(keycloakUserDTO.getEmail());
        profileDTO.setLastName(keycloakUserDTO.getLastName());
        profileDTO.setIdNational(idNational);
        return profileDTO;
    }

    public ResponseEntity<Boolean> checarIdp(String username) {
        String userId = buscarUsuarioPorUsername(username)
                .stream()
                .findFirst()
                .map(KeycloakUserDTO::getId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado: " + username));

        String url = keycloakUrl + "/admin/realms/" + realm + "/users/" + userId + "/federated-identity" ;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAdminToken());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<List<Map<String, Object>>> resp = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {
                    }
            );

            List<Map<String, Object>> body = resp.getBody();
            return ResponseEntity.ok(body != null && !body.isEmpty());
        } catch (HttpClientErrorException.NotFound e) {
            throw new UserNotFoundException("Usuário não existe no Keycloak (id: " + userId + ")", e);
        } catch (HttpStatusCodeException e) {
            throw new IllegalStateException("Falha ao consultar federated-identity: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        }
    }

    public ResponseEntity<Boolean> temSenha(String username) {
        String userId = buscarUsuarioPorUsername(username)
                .stream()
                .findFirst()
                .map(KeycloakUserDTO::getId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado: " + username));

        String url = keycloakUrl + "/admin/realms/" + realm + "/users/" + userId + "/credentials" ;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAdminToken());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<List<Map<String, Object>>> resp = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {
                    }
            );

            List<Map<String, Object>> body = resp.getBody();
            return ResponseEntity.ok(body != null && !body.isEmpty());

        } catch (HttpClientErrorException.NotFound e) {
            throw new UserNotFoundException("Usuário não existe no Keycloak (id: " + userId + ")", e);
        } catch (HttpStatusCodeException e) {
            throw new IllegalStateException("Falha ao consultar credentials: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        }
    }

}
