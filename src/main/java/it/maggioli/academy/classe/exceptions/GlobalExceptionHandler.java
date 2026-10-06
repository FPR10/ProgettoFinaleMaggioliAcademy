package it.maggioli.academy.classe.exceptions;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AnagraficaNotFoundException.class)
    public ProblemDetail handleAnagraficaNotFound(AnagraficaNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Anagrafica non trovata", ex.getMessage());
    }

    @ExceptionHandler(CodiceFiscaleDuplicatoException.class)
    public ProblemDetail handleCodiceFiscaleDuplicato(CodiceFiscaleDuplicatoException ex) {
        return problem(HttpStatus.CONFLICT, "Codice fiscale duplicato", ex.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        
        ProblemDetail body = problem(HttpStatus.BAD_REQUEST,
                "Errore di validazione", "Uno o piu campi della richiesta non sono validi.");
        List<ValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ValidationError(error.getField(), error.getDefaultMessage()))
                .toList();
        body.setProperty("errors", errors);
        return handleExceptionInternal(ex, body, headers, status, request);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpectedException(Exception ex) {
        log.error("Errore inatteso durante la gestione della richiesta", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR,
                "Errore interno", "Si e verificato un errore interno. Riprovare piu tardi.");
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(status, detail);
        body.setTitle(title);
        return body;
    }

    public record ValidationError(String field, String message) {}
}
