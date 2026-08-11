package com.lea.transport.pattern.strategy;

import com.lea.transport.exception.RecordLockedException;

/**
 * Strategy pattern (behavioural). PupilRecordService depends only on
 * this interface, not on any particular locking implementation, so the
 * concurrency-control algorithm can be swapped without touching calling
 * code - e.g. replacing InMemoryLockingStrategy with a distributed
 * implementation for a multi-instance deployment.
 */
public interface LockingStrategy {
    RecordLock acquire(String recordId, String holder) throws RecordLockedException;
    void release(String recordId, String holder);
    boolean isLocked(String recordId);
}
