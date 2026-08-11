package com.lea.transport.pattern.strategy;

import com.lea.transport.exception.RecordLockedException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Concrete LockingStrategy using an in-memory map guarded by a single
 * ReentrantLock around the check-then-act acquire operation.
 *
 * Justification: this prototype is a single-JVM console application with
 * no persistence layer - per the brief, data may be hard-coded. Every
 * concurrent "user" is a thread inside the same JVM, so an in-memory
 * structure is sufficient to guarantee mutual exclusion; a distributed
 * lock (database row-lock, Redis) would add real complexity - network
 * calls, lock expiry, heartbeats - that this deployment scope cannot
 * even exercise, since only one instance is ever running. Because
 * PupilRecordService depends only on the LockingStrategy interface, only
 * this class would need replacing were the system ever deployed across
 * multiple instances - a limitation flagged deliberately.
 */
public class InMemoryLockingStrategy implements LockingStrategy {
    private final ConcurrentHashMap<String, RecordLock> locks = new ConcurrentHashMap<>();
    private final ReentrantLock guard = new ReentrantLock();

    @Override
    public RecordLock acquire(String recordId, String holder) throws RecordLockedException {
        guard.lock();
        try {
            RecordLock existing = locks.get(recordId);
            if (existing != null && !existing.getHolder().equals(holder)) {
                throw new RecordLockedException(recordId, existing.getHolder());
            }
            RecordLock lock = new RecordLock(recordId, holder);
            locks.put(recordId, lock);
            return lock;
        } finally {
            guard.unlock();
        }
    }

    @Override
    public void release(String recordId, String holder) {
        guard.lock();
        try {
            RecordLock existing = locks.get(recordId);
            if (existing != null && existing.getHolder().equals(holder)) {
                locks.remove(recordId);
            }
        } finally {
            guard.unlock();
        }
    }

    @Override
    public boolean isLocked(String recordId) { return locks.containsKey(recordId); }
}
