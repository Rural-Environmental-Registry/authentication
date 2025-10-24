package hammerConsult.cardpg.controller;

import hammerConsult.cardpg.keycloak.controller.KeycloakController;
import hammerConsult.cardpg.keycloak.dto.KeycloakCredentialUserDTO;
import hammerConsult.cardpg.keycloak.dto.KeycloakUserDTO;
import hammerConsult.cardpg.keycloak.dto.ProfileDTO;
import hammerConsult.cardpg.keycloak.service.KeycloakService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(KeycloakController.class)
class KeycloakControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KeycloakService keycloakService;

    @Test
    void deveRetornarTokenAdmin() throws Exception {
        when(keycloakService.getAdminToken()).thenReturn("fake-token");

        mockMvc.perform(get("/api/keycloak/token"))
                .andExpect(status().isOk())
                .andExpect(content().string("fake-token"));
    }

    @Test
    void deveTratarCallbackGov() throws Exception {
        when(keycloakService.handleGovRedirect(eq("abc123")))
                .thenReturn(ResponseEntity.ok(Map.of("access_token", "xyz")));

        mockMvc.perform(post("/api/keycloak/auth/callback?code=abc123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("xyz"));
    }

    @Test
    void deveCriarUsuario() throws Exception {
        KeycloakCredentialUserDTO dto = new KeycloakCredentialUserDTO();
        dto.setValue("123");

        mockMvc.perform(post("/api/keycloak/usuario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\":\"123\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void deveBuscarUsuarioPorUsername() throws Exception {
        KeycloakUserDTO user = new KeycloakUserDTO();
        user.setUsername("maria");
        when(keycloakService.buscarUsuarioPorUsername("maria"))
                .thenReturn(List.of(user));

        mockMvc.perform(get("/api/keycloak/username/maria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("maria"));
    }

    @Test
    void deveBuscarUsuarioPorId() throws Exception {
        ProfileDTO profile = new ProfileDTO();
        profile.setId("10");
        when(keycloakService.buscarDadosUsuario("10")).thenReturn(profile);

        mockMvc.perform(get("/api/keycloak/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("10"));
    }

    @Test
    void deveResetarSenha() throws Exception {
        when(keycloakService.resetUserPassword(eq("123"), eq("novaSenha")))
                .thenReturn(ResponseEntity.ok("resetado"));

        mockMvc.perform(put("/api/keycloak/resetPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"123\",\"value\":\"novaSenha\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("resetado"));
    }

    @Test
    void deveCriarUsuarioCompleto() throws Exception {
        when(keycloakService.createUserComplete(any()))
                .thenReturn(ResponseEntity.ok("ok"));

        mockMvc.perform(post("/api/keycloak/createUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\":\"x\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    @Test
    void deveTratarPreflight() throws Exception {
        mockMvc.perform(options("/api/keycloak/qualquer"))
                .andExpect(status().isOk());
    }

    @Test
    void deveAtualizarPerfil() throws Exception {
        when(keycloakService.atualizarUsuario(any())).thenReturn(ResponseEntity.ok("atualizado"));

        mockMvc.perform(put("/api/keycloak/updatePerfil")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"teste\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("atualizado"));
    }

    @Test
    void deveChecarIdp() throws Exception {
        when(keycloakService.checarIdp("joao")).thenReturn(ResponseEntity.ok(true));

        mockMvc.perform(get("/api/keycloak/federated-identity/joao"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void deveChecarCredenciais() throws Exception {
        when(keycloakService.temSenha("maria")).thenReturn(ResponseEntity.ok(false));

        mockMvc.perform(get("/api/keycloak/credentials/maria"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
}
