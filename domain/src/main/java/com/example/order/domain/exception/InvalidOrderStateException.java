package com.example.order.domain.exception;

/**
 * Thrown when an operation is attempted on an Order in an inappropriate state.
 * This is a domain exception — no framework dependency.
 */
public class InvalidOrderStateException extends RuntimeException {

    public InvalidOrderStateException(String message) {
        super(message);
    }

    public InvalidOrderStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
