package com.lea.transport.exception;

/** Thrown when a record is already locked by a different user (ITT 3.e). */
public class RecordLockedException extends TransportSystemException {
    private final String recordId;
    private final String currentHolder;

    public RecordLockedException(String recordId, String currentHolder) {
        super(String.format("Record '%s' is currently locked by '%s'. Please try again shortly.",
                recordId, currentHolder));
        this.recordId = recordId;
        this.currentHolder = currentHolder;
    }

    public String getRecordId() { return recordId; }
    public String getCurrentHolder() { return currentHolder; }
}
