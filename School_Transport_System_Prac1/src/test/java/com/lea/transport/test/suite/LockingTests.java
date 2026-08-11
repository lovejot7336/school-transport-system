package com.lea.transport.test.suite;

import com.lea.transport.exception.RecordLockedException;
import com.lea.transport.pattern.strategy.InMemoryLockingStrategy;
import com.lea.transport.pattern.strategy.LockingStrategy;
import com.lea.transport.pattern.strategy.RecordLock;
import com.lea.transport.test.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.lea.transport.test.Assertions.*;

/** Covers the Strategy-pattern locking mechanism satisfying ITT requirement 3.e. */
public class LockingTests {

    @Test
    public void acquireThenReleaseAllowsReacquisitionByAnyone() throws Exception {
        LockingStrategy locking = new InMemoryLockingStrategy();
        RecordLock lock = locking.acquire("P1", "school-user-A");
        assertEquals("P1", lock.getRecordId(), "Lock should be for the requested record");
        assertTrue(locking.isLocked("P1"), "Record should report as locked after acquire");

        locking.release("P1", "school-user-A");
        assertFalse(locking.isLocked("P1"), "Record should report as unlocked after release");

        RecordLock secondLock = locking.acquire("P1", "school-user-B");
        assertEquals("school-user-B", secondLock.getHolder(), "New holder should now own the lock");
    }

    @Test
    public void sameHolderCanReacquireItsOwnLock() throws Exception {
        LockingStrategy locking = new InMemoryLockingStrategy();
        locking.acquire("P1", "school-user-A");
        RecordLock again = locking.acquire("P1", "school-user-A");
        assertEquals("school-user-A", again.getHolder(), "Same holder re-acquiring should succeed");
    }

    @Test
    public void secondUserCannotAcquireLockAlreadyHeldByAnotherUser() throws Exception {
        LockingStrategy locking = new InMemoryLockingStrategy();
        locking.acquire("P1", "school-user-A");

        RecordLockedException ex = assertThrows(RecordLockedException.class,
                () -> locking.acquire("P1", "school-user-B"),
                "A second, different holder must be rejected while the record is locked");
        assertEquals("school-user-A", ex.getCurrentHolder(), "Exception should report the current holder");
        assertEquals("P1", ex.getRecordId(), "Exception should report the locked record id");
    }

    @Test
    public void releaseByNonHolderIsIgnoredAndLockRemainsHeld() throws Exception {
        LockingStrategy locking = new InMemoryLockingStrategy();
        locking.acquire("P1", "school-user-A");
        locking.release("P1", "school-user-B");
        assertTrue(locking.isLocked("P1"), "Lock should remain held since release was requested by a non-holder");
    }

    /**
     * Real concurrency test: two threads race for the same lock. Exactly
     * one must succeed and the other must be rejected - proving mutual
     * exclusion holds under genuine thread contention, not just
     * sequential calls.
     */
    @Test
    public void concurrentThreadsRacingForSameLockOnlyOneSucceeds() throws Exception {
        LockingStrategy locking = new InMemoryLockingStrategy();
        CountDownLatch startLine = new CountDownLatch(1);
        AtomicBoolean threadASucceeded = new AtomicBoolean(false);
        AtomicBoolean threadBSucceeded = new AtomicBoolean(false);
        AtomicReference<Exception> unexpected = new AtomicReference<>();

        Runnable racerA = () -> {
            try {
                startLine.await();
                locking.acquire("P1", "thread-A");
                threadASucceeded.set(true);
            } catch (RecordLockedException expectedMaybe) {
            } catch (Exception e) {
                unexpected.set(e);
            }
        };
        Runnable racerB = () -> {
            try {
                startLine.await();
                locking.acquire("P1", "thread-B");
                threadBSucceeded.set(true);
            } catch (RecordLockedException expectedMaybe) {
            } catch (Exception e) {
                unexpected.set(e);
            }
        };

        Thread t1 = new Thread(racerA);
        Thread t2 = new Thread(racerB);
        t1.start();
        t2.start();
        startLine.countDown();
        t1.join();
        t2.join();

        assertNull(unexpected.get(), "No unexpected exception should occur during the race");
        assertTrue(threadASucceeded.get() ^ threadBSucceeded.get(),
                "Exactly one of the two racing threads should have won the lock, not zero or both");
    }
}
