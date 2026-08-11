package com.lea.transport.pattern.strategy;

import java.time.LocalDateTime;

/** A lease held by a user on a single record, preserving data integrity under multi-user access. */
public class RecordLock {
    private final String recordId;
    private final String holder;
    private final LocalDateTime acquiredAt;

    public RecordLock(String recordId, String holder) {
        this.recordId = recordId;
        this.holder = holder;
        this.acquiredAt = LocalDateTime.now();
    }

    public String getRecordId() { return recordId; }
    public String getHolder() { return holder; }
    public LocalDateTime getAcquiredAt() { return acquiredAt; }

    @Override
    public String toString() {
        return String.format("Lock[record=%s, holder=%s, since=%s]", recordId, holder, acquiredAt);
    }
}
