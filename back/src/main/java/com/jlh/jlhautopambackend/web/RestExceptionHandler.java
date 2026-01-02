package com.jlh.jlhautopambackend.web;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class RestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    private static Map<String, Object> body(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("message", message);
        return body;
    }

    // 400 - Bean Validation sur @RequestBody (RegisterRequest, etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> resp = body("Validation error");
        Map<String, Object> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> errors.put(fe.getField(), fe.getDefaultMessage()));
        resp.put("errors", errors);
        return ResponseEntity.badRequest().body(resp);
    }

    // 400 - Bean Validation sur @RequestParam / @PathVariable / @Validated services
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, Object> resp = body("Validation error");
        Map<String, Object> errors = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(cv -> {
            // Exemples de propertyPath: "register.arg0.email" ou "getById.id"
            String path = cv.getPropertyPath() != null ? cv.getPropertyPath().toString() : "param";
            errors.put(path, cv.getMessage());
        });
        resp.put("errors", errors);
        return ResponseEntity.badRequest().body(resp);
    }

    // 400 - mauvais type dans l’URL ou query param (ex: id="abc" attendu Long)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String name = ex.getName();
        String required = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "type";
        Map<String, Object> resp = body("Type mismatch");
        resp.put("errors", Map.of(name, "Type attendu: " + required));
        return ResponseEntity.badRequest().body(resp);
    }

    // 404 - entité non trouvée
    @ExceptionHandler({NoSuchElementException.class, EntityNotFoundException.class})
    public ResponseEntity<?> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body("Ressource introuvable"));
    }

    // 409 - contraintes d’unicité, FK, etc.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDup(DataIntegrityViolationException ex) {
        // On peut affiner selon le message SQL, mais gardons un mapping simple et utile côté front
        Map<String, Object> resp = body("Conflit de données");
        resp.put("errors", Map.of("email", "déjà utilisé"));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(resp);
    }

    // 500 - fallback générique
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAll(Exception ex) {
        // log serveur, message client générique
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body("Erreur interne. Réessayez plus tard."));
    }
}
