package com.lea.transport.exception;

/** Thrown when CSV import fails - wraps a checked IOException or a malformed-row failure. */
public class ImportFailedException extends TransportSystemException {
    public ImportFailedException(String message) { super(message); }
    public ImportFailedException(String message, Throwable cause) { super(message, cause); }
}
