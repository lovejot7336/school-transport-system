package com.lea.transport.exception;

/** Thrown when a pupil address fails validation, before any lock is acquired. */
public class InvalidAddressException extends TransportSystemException {
    public InvalidAddressException(String badAddress) {
        super(String.format("'%s' is not a valid address.", badAddress));
    }
}
