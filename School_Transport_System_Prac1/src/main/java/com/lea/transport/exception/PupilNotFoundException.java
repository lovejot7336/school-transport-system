package com.lea.transport.exception;

/** Thrown when an operation references a pupilId that does not exist. */
public class PupilNotFoundException extends TransportSystemException {
    public PupilNotFoundException(String pupilId) {
        super(String.format("No pupil found with id '%s'.", pupilId));
    }
}
