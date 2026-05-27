package com.wotos.wotosplayerservice.exception;

import java.time.LocalDateTime;

/**
 * Standard error payload returned for every exception handled by
 * {@link GlobalExceptionHandler}, so all controllers expose a consistent error contract.
 */
public class ErrorResponse {

    private final int status;
    private final String message;
    private final LocalDateTime timestamp;

    /**
     * @param status  the HTTP status code returned to the client
     * @param message human-readable description of the failure
     */
    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
