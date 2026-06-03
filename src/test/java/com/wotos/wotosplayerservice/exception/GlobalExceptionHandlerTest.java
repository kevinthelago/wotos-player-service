package com.wotos.wotosplayerservice.exception;

import feign.FeignException;
import feign.RetryableException;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pure unit tests for {@link GlobalExceptionHandler} — no Spring context or database required, so
 * each handler's status code and {@link ErrorResponse} body can be asserted in isolation. Every
 * response carries the {@code {"error":{"code","message","correlationId"}}} envelope.
 */
public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    public void handleEntityNotFoundReturns404() {
        ResponseEntity<ErrorResponse> response =
                handler.handleEntityNotFound(new EntityNotFoundException("player 123 not found"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error().code()).isEqualTo(404);
        assertThat(response.getBody().error().message()).isEqualTo("player 123 not found");
        assertThat(response.getBody().error().correlationId()).isNotBlank();
    }

    @Test
    public void handleValidationReturns400WithFieldDetail() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(
                Collections.singletonList(new FieldError("player", "language", "must be a supported language")));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().error().code()).isEqualTo(400);
        assertThat(response.getBody().error().message()).contains("language");
    }

    @Test
    public void handleConstraintViolationReturns400WithViolationDetail() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("must be less than or equal to 100");

        ConstraintViolationException ex = new ConstraintViolationException(
                Collections.singleton(violation));

        ResponseEntity<ErrorResponse> response = handler.handleConstraintViolation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().error().code()).isEqualTo(400);
        assertThat(response.getBody().error().message()).contains("must be less than or equal to 100");
    }

    @Test
    public void handleTimeoutReturns504() {
        RetryableException ex = mock(RetryableException.class);
        when(ex.getMessage()).thenReturn("connect timed out executing GET");

        ResponseEntity<ErrorResponse> response = handler.handleTimeout(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
        assertThat(response.getBody().error().code()).isEqualTo(504);
        assertThat(response.getBody().error().correlationId()).isNotBlank();
    }

    @Test
    public void handleFeignReturns502() {
        FeignException ex = mock(FeignException.class);
        when(ex.getMessage()).thenReturn("status 500 reading WotAccountsFeignClient");

        ResponseEntity<ErrorResponse> response = handler.handleFeign(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody().error().code()).isEqualTo(502);
    }

    @Test
    public void handleGenericReturns500() {
        ResponseEntity<ErrorResponse> response = handler.handleGeneric(new RuntimeException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().error().code()).isEqualTo(500);
        assertThat(response.getBody().error().message()).isEqualTo("An unexpected error occurred");
    }
}
