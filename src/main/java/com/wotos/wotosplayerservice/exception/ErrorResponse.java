package com.wotos.wotosplayerservice.exception;

import java.util.UUID;

/**
 * Standard error payload returned for every exception handled by
 * {@link GlobalExceptionHandler}. Serializes as
 * {@code {"error":{"code":<int>,"message":<string>,"correlationId":<uuid>}}} so all controllers
 * expose a consistent error contract and each failure carries a traceable correlation id.
 *
 * @param error the error detail wrapper
 */
public record ErrorResponse(ErrorDetail error) {

    /**
     * @param code          the HTTP status code returned to the client
     * @param message       human-readable description of the failure
     * @param correlationId a unique id for this error, echoed in the service logs for tracing
     */
    public record ErrorDetail(int code, String message, String correlationId) {
    }

    /**
     * Builds an {@link ErrorResponse} with the given code/message and the supplied correlation id.
     */
    public static ErrorResponse of(int code, String message, String correlationId) {
        return new ErrorResponse(new ErrorDetail(code, message, correlationId));
    }

    /**
     * Builds an {@link ErrorResponse} with a freshly generated correlation id.
     */
    public static ErrorResponse of(int code, String message) {
        return of(code, message, UUID.randomUUID().toString());
    }
}
