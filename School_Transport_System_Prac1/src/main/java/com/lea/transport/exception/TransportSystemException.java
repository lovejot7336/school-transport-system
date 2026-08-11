package com.lea.transport.exception;

/**
 * Root checked exception for all custom failures. Kept checked
 * deliberately: every failure that reaches the UI layer is one the user
 * can potentially retry, so forcing callers to handle it is a feature.
 */
public class TransportSystemException extends Exception {
    public TransportSystemException(String message) { super(message); }
    public TransportSystemException(String message, Throwable cause) { super(message, cause); }
}
