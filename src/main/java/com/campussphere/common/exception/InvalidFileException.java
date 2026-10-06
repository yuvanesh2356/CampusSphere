package com.campussphere.common.exception;

/**
 * Thrown when an uploaded file fails validation (disallowed type) or
 * cannot be stored due to an I/O failure. Shared under common/exception
 * since every module with image upload (Marketplace now; Freelance
 * posters, Guidance material, etc. in later phases) will reuse it.
 */
public class InvalidFileException extends RuntimeException {

    public InvalidFileException(String message) {
        super(message);
    }
}
