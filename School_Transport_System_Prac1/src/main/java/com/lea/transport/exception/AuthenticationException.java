package com.lea.transport.exception;

/** Thrown on a failed login attempt - unknown username or wrong password. */
public class AuthenticationException extends TransportSystemException {
    public AuthenticationException() { super("Invalid username or password."); }
}
