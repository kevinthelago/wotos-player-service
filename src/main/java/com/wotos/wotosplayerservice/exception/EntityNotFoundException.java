package com.wotos.wotosplayerservice.exception;

/**
 * Thrown when a requested player, snapshot, or achievement record cannot be found.
 *
 * <p>Deliberately distinct from {@link javax.persistence.EntityNotFoundException}: this is a
 * domain-level exception mapped to HTTP 404 by {@link GlobalExceptionHandler}, keeping
 * persistence-layer concerns out of the web layer's error contract.
 */
public class EntityNotFoundException extends RuntimeException {

    /**
     * @param message human-readable detail describing which entity was not found
     */
    public EntityNotFoundException(String message) {
        super(message);
    }
}
