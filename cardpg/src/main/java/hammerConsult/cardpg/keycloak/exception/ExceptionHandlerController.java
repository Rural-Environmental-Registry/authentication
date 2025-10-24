package hammerConsult.cardpg.keycloak.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHandlerController {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<String> handleinvalidPassword(InvalidPasswordException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
    @ExceptionHandler(ErrorGenericException.class)
    public ResponseEntity<String> handleErroGeneric(ErrorGenericException ex) {
        return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body(ex.getMessage());
    }
}
