package br.edu.ifpb.instagram.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(FieldAlreadyExistsException.class)
    public ResponseEntity<Object> handleEmailAlreadyExists(FieldAlreadyExistsException ex) {

        Map<String, String> errorResponse = Map.of(
            "error", "Conflict",
            "message", ex.getMessage()
        );

        // Retorna a resposta com o status HTTP 409 Conflict
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }
}
