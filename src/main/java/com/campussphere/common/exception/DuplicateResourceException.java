package com.campussphere.common.exception;

/**
 * Thrown when an operation would create a duplicate resource -
 * e.g. registering with an email address that already exists.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
