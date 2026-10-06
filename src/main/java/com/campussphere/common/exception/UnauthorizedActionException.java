package com.campussphere.common.exception;

/**
 * Thrown when an authenticated user attempts an action they are not
 * permitted to perform (e.g. a STUDENT attempting an ADMIN-only action).
 */
public class UnauthorizedActionException extends RuntimeException {

    public UnauthorizedActionException(String message) {
        super(message);
    }
}
