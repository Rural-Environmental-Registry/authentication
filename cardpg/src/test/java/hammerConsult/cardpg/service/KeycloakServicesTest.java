package hammerConsult.cardpg.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import hammerConsult.cardpg.keycloak.dto.KeycloakCredentialUserDTO;
import hammerConsult.cardpg.keycloak.dto.KeycloakUserDTO;
import hammerConsult.cardpg.keycloak.dto.ProfileDTO;
import hammerConsult.cardpg.keycloak.exception.ErrorGenericException;
import hammerConsult.cardpg.keycloak.exception.InvalidPasswordException;
import hammerConsult.cardpg.keycloak.exception.UserNotFoundException;
import hammerConsult.cardpg.keycloak.service.KeycloakService;
import hammerConsult.cardpg.keycloak.utils.JwtDecodeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeycloakServicesTest {

    @Mock
    private RestTemplate restTemplate;

    @Spy
    @InjectMocks
    private KeycloakService keycloakService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(keycloakService, "keycloakUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(keycloakService, "realm", "myrealm");
        ReflectionTestUtils.setField(keycloakService, "clientId", "my-client-id");
        ReflectionTestUtils.setField(keycloakService, "clientSecret", "my-secret");
        ReflectionTestUtils.setField(keycloakService, "baseUrl", "http://localhost:8081");

        // Garante que a instância de RestTemplate usada pela classe é o nosso mock
        keycloakService.restTemplate = restTemplate;
    }

    @Test
    void testHandleGovRedirectSuccess() {
        String code = "abc123";
        String fakeToken = "fake.jwt.token";

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(Map.of("access_token", fakeToken)));

        try (MockedStatic<JwtDecodeUtils> jwtUtils = mockStatic(JwtDecodeUtils.class)) {

            jwtUtils.when(() -> JwtDecodeUtils.decodeJwtClaims(fakeToken))
                    .thenReturn(Map.of("sub", "user-123"));
            jwtUtils.when(() -> JwtDecodeUtils.extractDataToken(anyMap()))
                    .thenReturn(new KeycloakCredentialUserDTO());

            ResponseEntity<Map<String, Object>> resp = keycloakService.handleGovRedirect(code);

            assertEquals(HttpStatus.OK, resp.getStatusCode());
            assertEquals(fakeToken, resp.getBody().get("access_token"));
        }
    }


    @Test
    void testBadGatewayException() {
        // arrange
        String code = "abc123";

        ResponseEntity<Map<String, Object>> responseEntity =
                new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any()))
                .thenReturn(responseEntity);

        // act
        ResponseEntity<Map<String, Object>> resp = keycloakService.handleGovRedirect(code);

        // assert
        assertEquals(HttpStatus.BAD_GATEWAY, resp.getStatusCode());
        assertTrue(resp.getBody().containsKey("error"));
    }

    @Test
    void testHttpClientErrorException() {
        // arrange
        String code = "abc123";

        HttpClientErrorException exception = new HttpClientErrorException(
                HttpStatus.UNAUTHORIZED, "Unauthorized");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any()))
                .thenThrow(exception);

        // act
        ResponseEntity<Map<String, Object>> resp = keycloakService.handleGovRedirect(code);

        // assert
        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
        assertTrue(resp.getBody().get("error").toString().contains("Erro ao trocar o code"));
    }

    @Test
    void testGetAdminTokenSuccess() {
        Map<String, String> tokenMap = Map.of("access_token", "fake-token");
        ResponseEntity<Map> mockResponse = new ResponseEntity<>(tokenMap, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class))).thenReturn(mockResponse);

        String token = keycloakService.getAdminToken();

        assertEquals("fake-token", token);
    }

    @Test
    void testGetAdminTokenFailure() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

        assertThrows(ErrorGenericException.class, () -> keycloakService.getAdminToken());
    }

    @Test
    void testCreateUserSuccess() {
        KeycloakCredentialUserDTO dto = new KeycloakCredentialUserDTO();
        dto.setEmail("user@test.com");
        dto.setFirstName("User");
        dto.setLastName("Test");
        dto.setIdNational("123456789");
        dto.setValue("password123");

        doReturn("fake-access-token").when(keycloakService).getAdminToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("http://localhost:8080/admin/realms/myrealm/users/abc123"));
        ResponseEntity<Void> postResponse = new ResponseEntity<>(headers, HttpStatus.CREATED);

        when(restTemplate.exchange(
                contains("/admin/realms/myrealm/users"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(postResponse);

        KeycloakUserDTO mockUser = new KeycloakUserDTO();
        mockUser.setId("abc123");
        mockUser.setUsername("normalUser");

        when(restTemplate.exchange(
                contains("/users/abc123"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(KeycloakUserDTO.class)
        )).thenReturn(ResponseEntity.ok(mockUser));

        when(restTemplate.exchange(
                contains("/users/abc123/reset-password"),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(ResponseEntity.ok().build());

        doReturn(false).when(keycloakService).isAdmin(anyString(), eq("normalUser"));

        String userId = keycloakService.createUser(dto);

        assertEquals("abc123", userId);
    }



    @Test
    void testCreateUserConflict() {
        var userDTO = new KeycloakCredentialUserDTO();
        userDTO.setEmail("conflict@example.com");

        doReturn("fake-access-token").when(keycloakService).getAdminToken();

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.CONFLICT, "Conflict", HttpHeaders.EMPTY, null, null));

        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class, () -> keycloakService.createUser(userDTO));
        assertEquals("Usuário já existe com esse username/email.", exception.getMessage());
    }

    @Test
    void testCreateUserFailureStatus() {
        KeycloakCredentialUserDTO dto = new KeycloakCredentialUserDTO();
        dto.setEmail("user@test.com");

        doReturn("fake-access-token").when(keycloakService).getAdminToken();

        ResponseEntity<Void> response = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(response);

        assertThrows(ErrorGenericException.class, () -> keycloakService.createUser(dto));
    }

    @Test
    void testCreateUserWithoutLocation() {
        KeycloakCredentialUserDTO dto = new KeycloakCredentialUserDTO();
        dto.setEmail("user@test.com");

        doReturn("fake-access-token").when(keycloakService).getAdminToken();

        ResponseEntity<Void> response = new ResponseEntity<>(new HttpHeaders(), HttpStatus.CREATED);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(response);

        assertThrows(ErrorGenericException.class, () -> keycloakService.createUser(dto));
    }

    @Test
    void testCreateUserHttpStatusCodeException() {
        KeycloakCredentialUserDTO dto = new KeycloakCredentialUserDTO();
        dto.setEmail("user@test.com");

        doReturn("fake-access-token").when(keycloakService).getAdminToken();

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request"));

        assertThrows(ErrorGenericException.class, () -> keycloakService.createUser(dto));
    }


    @Test
    void testBuscarUsuarioPorUsernameSuccess() {
        doReturn("fake-access-token").when(keycloakService).getAdminToken();
        List<KeycloakUserDTO> mockList = List.of(new KeycloakUserDTO());
        ResponseEntity<List<KeycloakUserDTO>> mockResponse = new ResponseEntity<>(mockList, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(mockResponse);

        List<KeycloakUserDTO> result = keycloakService.buscarUsuarioPorUsername("user123");
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testBuscarUsuarioPorUsernameError() {
        doReturn("fake-access-token").when(keycloakService).getAdminToken();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(UserNotFoundException.class, () -> keycloakService.buscarUsuarioPorUsername("qualquer"));
    }

    @Test
    void testBuscarUsuarioPorIdSuccess() {
        doReturn("fake-access-token").when(keycloakService).getAdminToken();
        var expectedUser = new KeycloakUserDTO();
        ResponseEntity<KeycloakUserDTO> mockResponse = new ResponseEntity<>(expectedUser, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(KeycloakUserDTO.class)))
                .thenReturn(mockResponse);

        KeycloakUserDTO result = keycloakService.buscarUsuarioPorId("123456");
        assertNotNull(result);
    }

    @Test
    void testBuscarUsuarioPorIdNotFound() {
        doReturn("fake-access-token").when(keycloakService).getAdminToken();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(KeycloakUserDTO.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThrows(UserNotFoundException.class, () -> keycloakService.buscarUsuarioPorId("not-found"));
    }

    @Test
    void testBuscarUsuarioPorIdNationalSuccess() {
        doReturn("fake-access-token").when(keycloakService).getAdminToken();
        List<KeycloakUserDTO> mockList = List.of(new KeycloakUserDTO());
        ResponseEntity<List<KeycloakUserDTO>> mockResponse = new ResponseEntity<>(mockList, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(mockResponse);

        List<KeycloakUserDTO> result = keycloakService.buscarUsuarioPorIdNational("123456");
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testBuscarUsuarioPorIdNationalError() {
        doReturn("fake-access-token").when(keycloakService).getAdminToken();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(UserNotFoundException.class, () -> keycloakService.buscarUsuarioPorIdNational("123456"));
    }

    @Test
    void testValidateIfExistSuccess() {
        String email = "teste@teste.com";
        String idNational = "123456789";

        doReturn(Collections.emptyList()).when(keycloakService).buscarUsuarioPorUsername(email);
        doReturn(Collections.emptyList()).when(keycloakService).buscarUsuarioPorIdNational(idNational);

        assertDoesNotThrow(() -> keycloakService.validateIfExist(email, idNational));
    }

    @Test
    void testValidateIfExistAlreadyExistEmail() {
        String email = "teste@teste.com";
        String idNational = "123456789";

        doReturn(List.of(new KeycloakUserDTO())).when(keycloakService).buscarUsuarioPorUsername(email);

        InvalidPasswordException ex = assertThrows(InvalidPasswordException.class, () -> keycloakService.validateIfExist(email, idNational));
        assertTrue(ex.getMessage().contains("email " + email));
    }

    @Test
    void testTemSenhaSuccessTrue() {
        String username = "teste";
        doReturn("fake-access-token").when(keycloakService).getAdminToken();

        var userDTO = new KeycloakUserDTO();
        userDTO.setId("123");
        doReturn(List.of(userDTO)).when(keycloakService).buscarUsuarioPorUsername(username);

        List<Map<String, Object>> credentials = List.of(Map.of("id", "cred1"));
        ResponseEntity<List<Map<String, Object>>> response = ResponseEntity.ok(credentials);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(response);

        ResponseEntity<Boolean> result = keycloakService.temSenha(username);
        assertTrue(result.getBody());
    }

    @Test
    void testTemSenhaSuccessFalse() {
        String username = "teste";
        doReturn("fake-access-token").when(keycloakService).getAdminToken();

        var userDTO = new KeycloakUserDTO();
        userDTO.setId("123");
        doReturn(List.of(userDTO)).when(keycloakService).buscarUsuarioPorUsername(username);

        ResponseEntity<List<Map<String, Object>>> response = ResponseEntity.ok(Collections.emptyList());

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(response);

        ResponseEntity<Boolean> result = keycloakService.temSenha(username);
        assertFalse(result.getBody());
    }

    @Test
    void testChecarIdpSuccessTrue() {
        String username = "teste";
        doReturn("fake-access-token").when(keycloakService).getAdminToken();

        var userDTO = new KeycloakUserDTO();
        userDTO.setId("123");
        doReturn(List.of(userDTO)).when(keycloakService).buscarUsuarioPorUsername(username);

        List<Map<String, Object>> idps = List.of(Map.of("provider", "google"));
        ResponseEntity<List<Map<String, Object>>> response = ResponseEntity.ok(idps);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(response);

        ResponseEntity<Boolean> result = keycloakService.checarIdp(username);
        assertTrue(result.getBody());
    }

    @Test
    void testCreateUserCompleteSuccess() {
        KeycloakCredentialUserDTO dto = new KeycloakCredentialUserDTO();
        dto.setEmail("user@test.com");
        dto.setIdNational("123456789");

        doNothing().when(keycloakService).validateIfExist(dto.getEmail(), dto.getIdNational());
        doReturn("abc123").when(keycloakService).createUser(dto);
        doNothing().when(keycloakService).saveUser("abc123", dto);

        ResponseEntity<String> response = keycloakService.createUserComplete(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(keycloakService).validateIfExist(dto.getEmail(), dto.getIdNational());
        verify(keycloakService).createUser(dto);
        verify(keycloakService).saveUser("abc123", dto);
    }


    @Test
    void testCreateUserCompleteErrorWhenUserIdIsBlank() {
        KeycloakCredentialUserDTO dto = new KeycloakCredentialUserDTO();
        dto.setEmail("user@test.com");
        dto.setIdNational("123456789");

        doNothing().when(keycloakService).validateIfExist(dto.getEmail(), dto.getIdNational());
        doReturn("   ").when(keycloakService).createUser(dto);

        assertThrows(ErrorGenericException.class,
                () -> keycloakService.createUserComplete(dto));
    }

    @Test
    void testCreateUserCompleteErrorWhenUserIdIsNull() {
        KeycloakCredentialUserDTO dto = new KeycloakCredentialUserDTO();
        dto.setEmail("user@test.com");
        dto.setIdNational("123456789");

        doNothing().when(keycloakService).validateIfExist(dto.getEmail(), dto.getIdNational());
        doReturn(null).when(keycloakService).createUser(dto);

        assertThrows(ErrorGenericException.class,
                () -> keycloakService.createUserComplete(dto));
    }

    @Test
    void testAtualizarUsuarioSuccess() {
        KeycloakUserDTO dto = new KeycloakUserDTO();
        dto.setId("abc123");
        dto.setUsername("normalUser");
        dto.setEmail("user@test.com");
        dto.setIdNational("123");

        KeycloakUserDTO existing = new KeycloakUserDTO();
        existing.setId("abc123");
        existing.setUsername("normalUser");
        existing.setEmail("user@test.com");
        existing.setIdNational("123");

        // stubbar métodos internos
        doReturn(existing).when(keycloakService).buscarUsuarioPorId("abc123");
        doReturn("aaa.bbb.ccc").when(keycloakService).getAdminToken();
        doReturn(false).when(keycloakService).isAdmin(anyString(), eq("normalUser"));
        doReturn(Collections.emptyList()).when(keycloakService).buscarUsuarioPorUsername("user@test.com");
        doReturn(Collections.emptyList()).when(keycloakService).buscarUsuarioPorIdNational("123");

        when(restTemplate.exchange(
                contains("/users/abc123"),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(ResponseEntity.ok().build());

        doNothing().when(keycloakService).atualizarUsuarioCore(dto, "abc123");

        ResponseEntity<String> resp = keycloakService.atualizarUsuario(dto);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("Usuario atualizado com Sucesso.", resp.getBody());
    }


    @Test
    void testAtualizarUsuarioUserNotFound() {
        KeycloakUserDTO dto = new KeycloakUserDTO();
        dto.setId("abc123");

        doReturn(null).when(keycloakService).buscarUsuarioPorId("abc123");

        assertThrows(IllegalArgumentException.class,
                () -> keycloakService.atualizarUsuario(dto));
    }

    @Test
    void testAtualizarUsuarioAdminForbidden() {
        KeycloakUserDTO dto = new KeycloakUserDTO();
        dto.setId("abc123");
        dto.setUsername("admin");
        dto.setEmail("admin@test.com");
        dto.setIdNational("123");

        KeycloakUserDTO existing = new KeycloakUserDTO();
        existing.setId("abc123");
        existing.setUsername("admin");

        doReturn(existing).when(keycloakService).buscarUsuarioPorId("abc123");
        doReturn("aaa.bbb.ccc").when(keycloakService).getAdminToken();
        doReturn(true).when(keycloakService).isAdmin(anyString(), anyString());
        doReturn(Collections.emptyList()).when(keycloakService).buscarUsuarioPorUsername("admin@test.com");
        doReturn(Collections.emptyList()).when(keycloakService).buscarUsuarioPorIdNational("123");

        ResponseEntity<String> resp = keycloakService.atualizarUsuario(dto);

        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
        assertTrue(resp.getBody().contains("não é permitido alterar o usuário Admin"));
    }

    @Test
    void testAtualizarUsuarioEmailAlreadyExists() {
        KeycloakUserDTO dto = new KeycloakUserDTO();
        dto.setId("abc123");
        dto.setEmail("duplicate@test.com");
        dto.setIdNational("123");

        KeycloakUserDTO existing = new KeycloakUserDTO();
        existing.setId("abc123");
        existing.setEmail("original@test.com");

        KeycloakUserDTO otherUser = new KeycloakUserDTO();
        otherUser.setId("xyz999");
        otherUser.setEmail("duplicate@test.com");

        doReturn(existing).when(keycloakService).buscarUsuarioPorId("abc123");
        doReturn(List.of(otherUser)).when(keycloakService).buscarUsuarioPorUsername("duplicate@test.com");

        assertThrows(InvalidPasswordException.class,
                () -> keycloakService.atualizarUsuario(dto));
    }

    @Test
    void testAtualizarUsuarioIdNationalAlreadyExists() {
        KeycloakUserDTO dto = new KeycloakUserDTO();
        dto.setId("abc123");
        dto.setEmail("user@test.com");
        dto.setIdNational("999");

        KeycloakUserDTO existing = new KeycloakUserDTO();
        existing.setId("abc123");
        existing.setEmail("original@test.com");
        existing.setIdNational("123");

        KeycloakUserDTO otherUser = new KeycloakUserDTO();
        otherUser.setId("xyz999");
        otherUser.setEmail("another@test.com");
        otherUser.setAttributes(Map.of("idNational", List.of("999")));

        doReturn(existing).when(keycloakService).buscarUsuarioPorId("abc123");
        doReturn(Collections.emptyList()).when(keycloakService).buscarUsuarioPorUsername("user@test.com");
        doReturn(List.of(otherUser)).when(keycloakService).buscarUsuarioPorIdNational("999");

        assertThrows(InvalidPasswordException.class,
                () -> keycloakService.atualizarUsuario(dto));
    }

    @Test
    void testAtualizarUsuarioCoreSuccess() {
        KeycloakUserDTO dto = new KeycloakUserDTO();
        dto.setFirstName("User");
        dto.setLastName("Test");
        dto.setEmail("user@test.com");
        dto.setIdNational("123");

        doNothing().when(restTemplate).put(anyString(), anyMap());

        assertDoesNotThrow(() -> keycloakService.atualizarUsuarioCore(dto, "abc123"));

        verify(restTemplate).put(contains("/v1/users/keycloak/abc123"), anyMap());
    }

    @Test
    void testAtualizarUsuarioCoreError() {
        KeycloakUserDTO dto = new KeycloakUserDTO();
        dto.setFirstName("User");

        doThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST))
                .when(restTemplate).put(anyString(), anyMap());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> keycloakService.atualizarUsuarioCore(dto, "abc123"));

        assertTrue(ex.getMessage().contains("Erro ao salvar usuário no Core"));
    }

    @Test
    void testBuscarDadosUsuarioWithIdNational() {
        KeycloakUserDTO user = new KeycloakUserDTO();
        user.setId("abc123");
        user.setFirstName("User");
        user.setLastName("Test");
        user.setEmail("user@test.com");
        user.setAttributes(Map.of("idNational", List.of("123")));

        doReturn(user).when(keycloakService).buscarUsuarioPorId("abc123");

        ProfileDTO profile = keycloakService.buscarDadosUsuario("abc123");

        assertEquals("abc123", profile.getId());
        assertEquals("User", profile.getFirstName());
        assertEquals("123", profile.getIdNational());
    }

    @Test
    void testBuscarDadosUsuarioWithoutIdNational() {
        KeycloakUserDTO user = new KeycloakUserDTO();
        user.setId("abc123");
        user.setFirstName("User");
        user.setLastName("Test");
        user.setEmail("user@test.com");
        user.setAttributes(Map.of()); // vazio

        doReturn(user).when(keycloakService).buscarUsuarioPorId("abc123");

        ProfileDTO profile = keycloakService.buscarDadosUsuario("abc123");

        assertNull(profile.getIdNational());
    }






}