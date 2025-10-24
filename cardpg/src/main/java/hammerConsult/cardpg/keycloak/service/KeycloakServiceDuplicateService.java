package hammerConsult.cardpg.keycloak.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import hammerConsult.cardpg.keycloak.adapter.CreateUserPasswordAdpter;
import hammerConsult.cardpg.keycloak.dto.KeycloakCredentialUserDTO;
import hammerConsult.cardpg.keycloak.dto.KeycloakUserDTO;
import hammerConsult.cardpg.keycloak.dto.PasswordResetRequest;
import hammerConsult.cardpg.keycloak.exception.ErrorGenericException;
import hammerConsult.cardpg.keycloak.exception.InvalidPasswordException;
import hammerConsult.cardpg.keycloak.exception.UserNotFoundException;

public class KeycloakServiceDuplicateService {

	private String keycloakUrl = "http://localhost:8080/rechml/keycloak";

	private String realm = "car-dpg";

	private String clientId = "api-service";

	private String clientSecret = "YvM5Ymcx5zjcaWAhK4ER2eJw7nI7H50P";

	private  RestTemplate restTemplate = new RestTemplate();

    public KeycloakServiceDuplicateService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
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
	public ResponseEntity<Map<String, Object>> handleGovRedirect(@RequestParam("code") String code) {
	    String tokenUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";

	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

	    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
	    body.add("grant_type", "authorization_code");
	    body.add("code", code);
	    body.add("client_id", clientId);
	    body.add("client_secret", clientSecret);
	    body.add("redirect_uri", "https://inovacao.dataprev.gov.br/rechml");

	    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

	    try {
	        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
	        return ResponseEntity.ok(response.getBody());
	    } catch (HttpClientErrorException e) {
	        Map<String, Object> error = new HashMap<>();
	        error.put("error", "Erro ao trocar o code pelo token");
	        error.put("status", e.getStatusCode().value());
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
	    }
	}

	public ResponseEntity<String> createUser(@RequestBody KeycloakCredentialUserDTO userDTO) {
		String accessToken = getAdminToken(); // Usa o método que você já tem

		String createUserUrl = keycloakUrl + "/admin/realms/" + realm + "/users";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(accessToken);

		// Constrói o corpo com os dados recebidos
		Map<String, Object> user = new HashMap<>();
		user.put("username", userDTO.getEmail());
		user.put("enabled", Boolean.TRUE);
		user.put("firstName", userDTO.getFirstName());
		user.put("lastName", userDTO.getLastName());
		user.put("email", userDTO.getEmail());

		HttpEntity<Map<String, Object>> request = new HttpEntity<>(user, headers);

		try {
			restTemplate.postForEntity(createUserUrl, request, String.class);
			return ResponseEntity.ok("Usuário criado com sucesso!");
		} catch (HttpClientErrorException.Conflict e) {
			throw new InvalidPasswordException("Usuario já existe com esse username");
		}
	}

	public ResponseEntity<String> resetUserPassword(String userId, String newPassword) {
		String resetPasswordUrl = keycloakUrl + "/admin/realms/" + realm + "/users/" + userId + "/reset-password";

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
	public List<KeycloakUserDTO> buscarUsuarioPorUsername(String usernameBody) {
		String createUserUrl = keycloakUrl + "/admin/realms/" + realm + "/users?username=" + usernameBody;

		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(getAdminToken());
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> entity = new HttpEntity<>(headers);

		try {
			ResponseEntity<List<KeycloakUserDTO>> response = restTemplate.exchange(
					createUserUrl,
					HttpMethod.GET,
					entity,
					new ParameterizedTypeReference<List<KeycloakUserDTO>>() {
					});

			if (response.getBody() == null || response.getBody().isEmpty()) {
				throw new UserNotFoundException("Id do Usuario: " + usernameBody + " não encontrado.");
			}
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
            e.printStackTrace();
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
}
