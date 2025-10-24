package hammerConsult.cardpg.service;

import hammerConsult.cardpg.keycloak.dto.KeycloakCredentialUserDTO;
import hammerConsult.cardpg.keycloak.dto.KeycloakUserDTO;
import hammerConsult.cardpg.keycloak.exception.ErrorGenericException;
import hammerConsult.cardpg.keycloak.exception.InvalidPasswordException;
import hammerConsult.cardpg.keycloak.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import hammerConsult.cardpg.keycloak.service.KeycloakServiceDuplicateService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeycloakServiceDuplicateTest {

    @Mock
    private RestTemplate restTemplate;

    private KeycloakCredentialUserDTO userDTO;

    @Test
    void testGetAdminTokenSuccess() {
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        KeycloakServiceDuplicateService service = new KeycloakServiceDuplicateService(mockRestTemplate);

        // Preencha os valores esperados
        ReflectionTestUtils.setField(service, "keycloakUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(service, "realm", "myrealm");
        ReflectionTestUtils.setField(service, "clientId", "my-client-id");
        ReflectionTestUtils.setField(service, "clientSecret", "my-secret");

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("access_token", "fake-token");

        ResponseEntity<Map> mockResponse = new ResponseEntity<>(tokenMap, HttpStatus.OK);
        when(mockRestTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(mockResponse);

        String token = service.getAdminToken();

        assertEquals("fake-token", token);
    }

    @Test
    void testGetAdminTokenFailure() {

        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        KeycloakServiceDuplicateService service = new KeycloakServiceDuplicateService(mockRestTemplate);

        ReflectionTestUtils.setField(service, "keycloakUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(service, "realm", "myrealm");
        ReflectionTestUtils.setField(service, "clientId", "my-client-id");
        ReflectionTestUtils.setField(service, "clientSecret", "my-secret");

        when(mockRestTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

        ErrorGenericException exception = assertThrows(
                ErrorGenericException.class,
                service::getAdminToken
        );

        assertTrue(exception.getMessage().contains("Erro ao obter token com client_credentials"));
    }


    @Test
    void testHandleGovRedirectSuccess() {

        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        KeycloakServiceDuplicateService service = new KeycloakServiceDuplicateService(mockRestTemplate);

        ReflectionTestUtils.setField(service, "keycloakUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(service, "realm", "myrealm");
        ReflectionTestUtils.setField(service, "clientId", "my-client-id");
        ReflectionTestUtils.setField(service, "clientSecret", "my-secret");

        Map<String, Object> tokenResponse = new HashMap<>();
        tokenResponse.put("access_token", "fake-access-token");

        ResponseEntity<Map> mockResponse = new ResponseEntity<>(tokenResponse, HttpStatus.OK);

        when(mockRestTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(mockResponse);

        ResponseEntity<Map<String, Object>> response = service.handleGovRedirect("auth-code-123");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("fake-access-token", response.getBody().get("access_token"));
    }

    @Test
    void testHandleGovRedirectFailure() {
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        KeycloakServiceDuplicateService service = new KeycloakServiceDuplicateService(mockRestTemplate);

        ReflectionTestUtils.setField(service, "keycloakUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(service, "realm", "myrealm");
        ReflectionTestUtils.setField(service, "clientId", "my-client-id");
        ReflectionTestUtils.setField(service, "clientSecret", "my-secret");

        when(mockRestTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        ResponseEntity<Map<String, Object>> response = service.handleGovRedirect("invalid-code");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Erro ao trocar o code pelo token", response.getBody().get("error"));
        assertEquals(401, response.getBody().get("status"));
    }


    @Test
    void testCreateUserSuccess() {
        userDTO = new KeycloakCredentialUserDTO();
        userDTO.setEmail("sucess@example.com");
        userDTO.setFirstName("Sucess");
        userDTO.setLastName("Test");

        KeycloakServiceDuplicateService realService = new KeycloakServiceDuplicateService(restTemplate);
        KeycloakServiceDuplicateService keycloakService = Mockito.spy(realService);

        doReturn("fake-access-token").when(keycloakService).getAdminToken();

        // Mock da criação de usuário
        ResponseEntity<String> mockResponse = new ResponseEntity<>("Created", HttpStatus.CREATED);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockResponse);

        ResponseEntity<String> response = keycloakService.createUser(userDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Usuário criado com sucesso!", response.getBody());
    }


    @Test
    void testCreateUserConflict() {

        userDTO = new KeycloakCredentialUserDTO();
        userDTO.setEmail("conflict@example.com");
        userDTO.setFirstName("conflict");
        userDTO.setLastName("Test");
        KeycloakServiceDuplicateService realService = new KeycloakServiceDuplicateService(restTemplate);
        KeycloakServiceDuplicateService keycloakService = Mockito.spy(realService);

        // Mock do token
        doReturn("fake-access-token").when(keycloakService).getAdminToken();

        // Simula conflito na criação do usuário
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.CONFLICT,
                        "Conflict",
                        HttpHeaders.EMPTY,
                        null,
                        null
                ));


        InvalidPasswordException exception = assertThrows(
                InvalidPasswordException.class,
                () -> keycloakService.createUser(userDTO)
        );

        assertEquals("Usuario já existe com esse username", exception.getMessage());
    }


    @Test
    void testResetUserPasswordSuccess() {
        KeycloakServiceDuplicateService keycloakService = Mockito.spy(new KeycloakServiceDuplicateService(restTemplate));

        // Mock do token
        doReturn("fake-access-token").when(keycloakService).getAdminToken();

        // Simula resposta do exchange com status 200 OK
        ResponseEntity<Void> mockResponse = new ResponseEntity<>(HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Void.class))
        ).thenReturn(mockResponse);

        ResponseEntity<String> response = keycloakService.resetUserPassword("user123", "NovaSenha@2025");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Senha alterada com sucesso.", response.getBody());
    }

    @Test
    void testResetUserPasswordInvalidPassword() {
        KeycloakServiceDuplicateService keycloakService = Mockito.spy(new KeycloakServiceDuplicateService(restTemplate));

        doReturn("fake-access-token").when(keycloakService).getAdminToken();

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Void.class))
        ).thenThrow(HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                HttpHeaders.EMPTY,
                null,
                null
        ));

        InvalidPasswordException exception = assertThrows(
                InvalidPasswordException.class,
                () -> keycloakService.resetUserPassword("user123", "senha")
        );

        assertEquals("A senha deve conter pelo menos oito caracteres, incluindo uma letra maiúscula, uma letra minúscula, um número e um caractere especial, como por exemplo, Segura#2025", exception.getMessage());
    }

    @Test
    void testBuscarUsuarioPorUsernameSuccess() {
        KeycloakServiceDuplicateService keycloakService = Mockito.spy(new KeycloakServiceDuplicateService(restTemplate));

        doReturn("fake-access-token").when(keycloakService).getAdminToken();

        List<KeycloakUserDTO> mockList = List.of(new KeycloakUserDTO());
        ResponseEntity<List<KeycloakUserDTO>> mockResponse = new ResponseEntity<>(mockList, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<List<KeycloakUserDTO>>>any())
        ).thenReturn(mockResponse);

        List<KeycloakUserDTO> result = keycloakService.buscarUsuarioPorUsername("user123");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testBuscarUsuarioPorUsernameNotFound() {
        KeycloakServiceDuplicateService keycloakService = Mockito.spy(new KeycloakServiceDuplicateService(restTemplate));

        doReturn("fake-access-token").when(keycloakService).getAdminToken();

        ResponseEntity<List<KeycloakUserDTO>> mockResponse = new ResponseEntity<>(List.of(), HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<List<KeycloakUserDTO>>>any())
        ).thenReturn(mockResponse);

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> keycloakService.buscarUsuarioPorUsername("userNotFound")
        );

        assertEquals("Id do Usuario: userNotFound não encontrado.", exception.getMessage());
    }


    @Test
    void testBuscarUsuarioPorUsernameUnexpectedError() {
        KeycloakServiceDuplicateService keycloakService = Mockito.spy(new KeycloakServiceDuplicateService(restTemplate));

        doReturn("fake-access-token").when(keycloakService).getAdminToken();

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<List<KeycloakUserDTO>>>any())
        ).thenThrow(HttpClientErrorException.create(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro Interno",
                HttpHeaders.EMPTY,
                null,
                null
        ));

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> keycloakService.buscarUsuarioPorUsername("qualquer")
        );

        assertEquals("Erro Inesperado ao buscar usuario", exception.getMessage());
    }

    @Test
    void testBuscarUsuarioPorIdNationalSuccess() {
        KeycloakServiceDuplicateService keycloakService = Mockito.spy(new KeycloakServiceDuplicateService(restTemplate));

        doReturn("fake-access-token").when(keycloakService).getAdminToken();

        List<KeycloakUserDTO> mockList = List.of(new KeycloakUserDTO());
        ResponseEntity<List<KeycloakUserDTO>> mockResponse = new ResponseEntity<>(mockList, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<List<KeycloakUserDTO>>>any())
        ).thenReturn(mockResponse);

        List<KeycloakUserDTO> result = keycloakService.buscarUsuarioPorIdNational("123456");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testBuscarUsuarioPorNationalIdUnexpectedError() {
        KeycloakServiceDuplicateService keycloakService = Mockito.spy(new KeycloakServiceDuplicateService(restTemplate));

        doReturn("fake-access-token").when(keycloakService).getAdminToken();

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<List<KeycloakUserDTO>>>any())
        ).thenThrow(HttpClientErrorException.create(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro Interno",
                HttpHeaders.EMPTY,
                null,
                null
        ));

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> keycloakService.buscarUsuarioPorIdNational("qualquer")
        );

        assertEquals("Erro inesperado ao buscar usuário por idNational", exception.getMessage());
    }

    @Test
    void testBuscarUsuarioPorIdSuccess() {
        KeycloakServiceDuplicateService keycloakService = Mockito.spy(new KeycloakServiceDuplicateService(restTemplate));

        doReturn("fake-access-token").when(keycloakService).getAdminToken();

        ResponseEntity<KeycloakUserDTO> mockResponse = new ResponseEntity<>(new KeycloakUserDTO(), HttpStatus.OK);

        when(restTemplate.exchange(
                        anyString(),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(KeycloakUserDTO.class)
                )
        ).thenReturn(mockResponse);

        KeycloakUserDTO result = keycloakService.buscarUsuarioPorId("123456");

        assertNotNull(result);
    }

    @Test
    void testBuscarUsuarioPorIdUnexpectedError() {
        KeycloakServiceDuplicateService keycloakService = Mockito.spy(new KeycloakServiceDuplicateService(restTemplate));

        doReturn("fake-access-token").when(keycloakService).getAdminToken();

        when(restTemplate.exchange(
                        anyString(),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(KeycloakUserDTO.class)
                )
        ).thenThrow(HttpClientErrorException.create(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro Interno",
                HttpHeaders.EMPTY,
                null,
                null
        ));

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> keycloakService.buscarUsuarioPorId("qualquer")
        );

        assertEquals("Erro ao buscar usuário com ID: qualquer", exception.getMessage());
    }

    @Test
    void testValidateIfExistSuccess() {
        KeycloakServiceDuplicateService keycloakService = Mockito.spy(new KeycloakServiceDuplicateService(restTemplate));

        String email = "teste@teste.com";
        String idNational = "123456789";

        doReturn(Collections.emptyList()).when(keycloakService).buscarUsuarioPorUsername(email);
        doReturn(Collections.emptyList()).when(keycloakService).buscarUsuarioPorIdNational(idNational);

        assertDoesNotThrow(() -> keycloakService.validateIfExist(email, idNational));
    }

    @Test
    void testValidateIfExistAlreadyExistEmail() {
        KeycloakServiceDuplicateService keycloakService = Mockito.spy(new KeycloakServiceDuplicateService(restTemplate));

        String email = "teste@teste.com";
        String idNational = "123456789";

        doReturn(List.of(new KeycloakUserDTO())).when(keycloakService).buscarUsuarioPorUsername(email);

        InvalidPasswordException ex = assertThrows(
                InvalidPasswordException.class,
                () -> keycloakService.validateIfExist(email, idNational)
        );

        assertTrue(ex.getMessage().contains("email " + email));
    }


    @Test
    void testValidateIfExistAlreadyExistIdNational() {
        KeycloakServiceDuplicateService keycloakService = Mockito.spy(new KeycloakServiceDuplicateService(restTemplate));

        String email = "teste@teste.com";
        String idNational = "123456789";

        doReturn(Collections.emptyList()).when(keycloakService).buscarUsuarioPorUsername(email);
        doReturn(List.of(new KeycloakUserDTO())).when(keycloakService).buscarUsuarioPorIdNational(idNational);


        InvalidPasswordException ex = assertThrows(
                InvalidPasswordException.class,
                () -> keycloakService.validateIfExist(email, idNational)
        );

        assertTrue(ex.getMessage().contains("idNational " + idNational));
    }


    @Test
    void testCredentialSuccessTrue() {
        KeycloakServiceDuplicateService keycloakService = Mockito.spy(new KeycloakServiceDuplicateService(restTemplate));
        doReturn("fake-access-token").when(keycloakService).getAdminToken();

        String username = "teste";

        KeycloakUserDTO userDTO = new KeycloakUserDTO();
        userDTO.setId("123");
        doReturn(List.of(userDTO)).when(keycloakService).buscarUsuarioPorUsername(username);

        List<Map<String, Object>> credentials = List.of(Map.of("id", "cred1"));
        ResponseEntity<List<Map<String, Object>>> response = ResponseEntity.ok(credentials);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<List<Map<String, Object>>>>any())
        ).thenReturn(response);

        ResponseEntity<Boolean> result = keycloakService.temSenha(username);

        assertTrue(result.getBody());
    }

    @Test
    void testCredentialSuccessFalse() {
        KeycloakServiceDuplicateService keycloakService = Mockito.spy(new KeycloakServiceDuplicateService(restTemplate));
        doReturn("fake-access-token").when(keycloakService).getAdminToken();

        String username = "teste";

        KeycloakUserDTO userDTO = new KeycloakUserDTO();
        userDTO.setId("123");
        doReturn(List.of(userDTO)).when(keycloakService).buscarUsuarioPorUsername(username);

        ResponseEntity<List<Map<String, Object>>> response = ResponseEntity.ok(Collections.emptyList());

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<List<Map<String, Object>>>>any())
        ).thenReturn(response);

        ResponseEntity<Boolean> result = keycloakService.temSenha(username);

        assertFalse(result.getBody());
    }

    @Test
    void testCredentialFailed() {
        KeycloakServiceDuplicateService keycloakService = Mockito.spy(new KeycloakServiceDuplicateService(restTemplate));

        doReturn("fake-access-token").when(keycloakService).getAdminToken();

        when(restTemplate.exchange(
                        anyString(),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        ArgumentMatchers.<ParameterizedTypeReference<List<Map<String, Object>>>>any()
                )
        ).thenThrow(HttpClientErrorException.create(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro Interno",
                HttpHeaders.EMPTY,
                null,
                null
        ));

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> keycloakService.temSenha("qualquer")
        );

        assertEquals("Erro Inesperado ao buscar usuario", exception.getMessage());
    }

    @Test
    void testCheckIdpSuccessTrue() {
        KeycloakServiceDuplicateService keycloakService = Mockito.spy(new KeycloakServiceDuplicateService(restTemplate));
        doReturn("fake-access-token").when(keycloakService).getAdminToken();

        String username = "teste";

        KeycloakUserDTO userDTO = new KeycloakUserDTO();
        userDTO.setId("123");
        doReturn(List.of(userDTO)).when(keycloakService).buscarUsuarioPorUsername(username);

        List<Map<String, Object>> credentials = List.of(Map.of("id", "cred1"));
        ResponseEntity<List<Map<String, Object>>> response = ResponseEntity.ok(credentials);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<List<Map<String, Object>>>>any())
        ).thenReturn(response);

        ResponseEntity<Boolean> result = keycloakService.checarIdp(username);

        assertTrue(result.getBody());
    }

    @Test
    void testCheckIdpSuccessFalse() {
        KeycloakServiceDuplicateService keycloakService = Mockito.spy(new KeycloakServiceDuplicateService(restTemplate));
        doReturn("fake-access-token").when(keycloakService).getAdminToken();

        String username = "teste";

        KeycloakUserDTO userDTO = new KeycloakUserDTO();
        userDTO.setId("123");
        doReturn(List.of(userDTO)).when(keycloakService).buscarUsuarioPorUsername(username);

        ResponseEntity<List<Map<String, Object>>> response = ResponseEntity.ok(Collections.emptyList());

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<List<Map<String, Object>>>>any())
        ).thenReturn(response);

        ResponseEntity<Boolean> result = keycloakService.checarIdp(username);

        assertFalse(result.getBody());
    }

    @Test
    void testCheckIdpFailed() {
        KeycloakServiceDuplicateService keycloakService = Mockito.spy(new KeycloakServiceDuplicateService(restTemplate));

        doReturn("fake-access-token").when(keycloakService).getAdminToken();

        when(restTemplate.exchange(
                        anyString(),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        ArgumentMatchers.<ParameterizedTypeReference<List<Map<String, Object>>>>any()
                )
        ).thenThrow(HttpClientErrorException.create(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro Interno",
                HttpHeaders.EMPTY,
                null,
                null
        ));

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> keycloakService.checarIdp("qualquer")
        );

        assertEquals("Erro Inesperado ao buscar usuario", exception.getMessage());
    }


}
