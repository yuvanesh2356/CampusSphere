package com.campussphere.common.exception;

/**
 * Thrown when a requested resource (e.g. a User) cannot be found.
 * Later phases (Listing, Interaction, etc.) will reuse this same
 * exception rather than defining a new "NotFound" exception per module.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
