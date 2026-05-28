package com.wotos.wotosplayerservice.exception;

import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * Centralizes exception handling across all controllers so that error responses share the
 * {@link ErrorResponse} schema and consistent HTTP status codes, keeping controllers free of
 * per-endpoint try/catch blocks.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maps a missing domain entity to {@code 404 Not Found}.
     *
     * @param ex the thrown {@link EntityNotFoundException}
     * @return an {@link ErrorResponse} carrying the exception's detail message
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Maps bean-validation failures on request bodies to {@code 400 Bad Request}, summarizing
     * each rejected field.
     *
     * @param ex the validation failure raised by Spring MVC
     * @return an {@link ErrorResponse} listing the rejected fields and their messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        if (message.isEmpty()) {
            message = "Validation failed";
        }

        return build(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * Maps bean-validation failures on request parameters (controllers annotated with
     * {@code @Validated}, e.g. {@code @RequestParam} + {@code @Max}/{@code @Language}) to
     * {@code 400 Bad Request}, summarizing each violated constraint.
     *
     * <p>Without this handler such failures would fall through to the generic 500.
     *
     * @param ex the constraint violation raised by Spring's parameter validation
     * @return an {@link ErrorResponse} listing each violated parameter and its message
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(", "));

        if (message.isEmpty()) {
            message = "Validation failed";
        }

        return build(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * Maps failures calling the downstream Wargaming/WoT API to {@code 502 Bad Gateway}.
     *
     * @param ex the {@link FeignException} raised by a WoT Feign client
     * @return an {@link ErrorResponse} indicating an upstream failure
     */
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeign(FeignException ex) {
        return build(HttpStatus.BAD_GATEWAY, "Upstream WoT API call failed: " + ex.getMessage());
    }

    /**
     * Catch-all for unhandled exceptions, mapped to {@code 500 Internal Server Error}. The raw
     * message is intentionally not leaked to the client.
     *
     * @param ex the unhandled exception
     * @return a generic {@link ErrorResponse}
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), message));
    }
}
