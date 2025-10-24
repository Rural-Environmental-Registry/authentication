package hammerConsult.cardpg.exception;


import hammerConsult.cardpg.keycloak.exception.ErrorGenericException;
import hammerConsult.cardpg.keycloak.exception.ExceptionHandlerController;
import hammerConsult.cardpg.keycloak.exception.InvalidPasswordException;
import hammerConsult.cardpg.keycloak.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RestController
class DummyController {
    @GetMapping("/user-not-found")
    public ResponseEntity<Void> throwUserNotFound() {
        throw new UserNotFoundException("Usuário não encontrado");
    }
    @GetMapping("/invalid-password")
    public ResponseEntity<Void> throwInvalidPassword() {
        throw new InvalidPasswordException("Senha inválida");
    }
    @GetMapping("/error-generic")
    public ResponseEntity<Void> throwErrorGeneric() {
        throw new ErrorGenericException("Erro genérico");
    }
}

@WebMvcTest(controllers = DummyController.class)
@Import(ExceptionHandlerController.class)
class ExceptionHandlerControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveRetornarNotFoundQuandoUserNotFoundException() throws Exception {
        mockMvc.perform(get("/user-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Usuário não encontrado"));
    }

    @Test
    void deveRetornarBadRequestQuandoInvalidPasswordException() throws Exception {
        mockMvc.perform(get("/invalid-password"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Senha inválida"));
    }

    @Test
    void deveRetornarPreconditionFailedQuandoErrorGenericException() throws Exception {
        mockMvc.perform(get("/error-generic"))
                .andExpect(status().isPreconditionFailed())
                .andExpect(content().string("Erro genérico"));
    }
}
