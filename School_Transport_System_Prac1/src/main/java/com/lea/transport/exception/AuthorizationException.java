package com.lea.transport.exception;

/** Thrown when an authenticated user attempts an action their role does not permit. */
public class AuthorizationException extends TransportSystemException {
    public AuthorizationException(String role, String action) {
        super(String.format("Role '%s' is not authorised to perform '%s'.", role, action));
    }
}
