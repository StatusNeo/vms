package com.statusneo.vms.exception;

/**
 * Exception thrown when directory synchronization operations fail.
 * This exception is used to indicate failures during Microsoft Graph API
 * calls or other directory sync operations.
 */
public class DirectorySyncException extends RuntimeException {

    public DirectorySyncException(String message) {
        super(message);
    }

    public DirectorySyncException(String message, Throwable cause) {
        super(message, cause);
    }
}

