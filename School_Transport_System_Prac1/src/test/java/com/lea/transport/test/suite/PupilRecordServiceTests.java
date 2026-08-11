package com.lea.transport.test.suite;

import com.lea.transport.exception.InvalidAddressException;
import com.lea.transport.exception.PupilNotFoundException;
import com.lea.transport.exception.RecordLockedException;
import com.lea.transport.model.ParentContact;
import com.lea.transport.model.Pupil;
import com.lea.transport.model.School;
import com.lea.transport.model.SchoolType;
import com.lea.transport.pattern.strategy.InMemoryLockingStrategy;
import com.lea.transport.pattern.strategy.LockingStrategy;
import com.lea.transport.service.DataRepository;
import com.lea.transport.service.PupilRecordService;
import com.lea.transport.test.Test;

import static com.lea.transport.test.Assertions.*;

/** Covers "Edit Pupil & Parent Details «include» Acquire Record Lock" - happy and failing paths. */
public class PupilRecordServiceTests {

    private DataRepository newRepositoryWithOnePupil() {
        DataRepository repo = new DataRepository();
        School school = new School("HS1", "Oakfield High School", SchoolType.HIGH, "Mrs. Carter");
        repo.addSchool(school);
        ParentContact guardian = new ParentContact("G1", "Sarah Jenkins", "sarah@example.com", "07700111222");
        Pupil pupil = new Pupil("P1", "Tom Jenkins", 11, "12 Elm Street", guardian);
        pupil.setSchool(school);
        repo.addPupil(pupil);
        return repo;
    }

    @Test
    public void editingNameAndYearGroupUpdatesThePupil() throws Exception {
        DataRepository repo = newRepositoryWithOnePupil();
        PupilRecordService service = new PupilRecordService(repo, new InMemoryLockingStrategy());
        boolean addressChanged = service.editPupilDetails("school-user", "P1", "Thomas Jenkins", 12, null);
        assertFalse(addressChanged, "Address was not part of this edit, so it should report unchanged");
        Pupil updated = repo.getPupil("P1");
        assertEquals("Thomas Jenkins", updated.getName(), "Name should be updated");
        assertEquals(12, updated.getYearGroup(), "Year group should be updated");
        assertEquals("12 Elm Street", updated.getHomeAddress(), "Address should be unchanged");
    }

    @Test
    public void editingAddressReportsAddressChangedTrue() throws Exception {
        DataRepository repo = newRepositoryWithOnePupil();
        PupilRecordService service = new PupilRecordService(repo, new InMemoryLockingStrategy());
        boolean addressChanged = service.editPupilDetails("school-user", "P1", null, null, "99 New Road");
        assertTrue(addressChanged, "Changing the address should report addressChanged = true");
        assertEquals("99 New Road", repo.getPupil("P1").getHomeAddress(), "Address should be updated");
    }

    @Test
    public void lockIsReleasedAfterASuccessfulEdit() throws Exception {
        DataRepository repo = newRepositoryWithOnePupil();
        LockingStrategy locking = new InMemoryLockingStrategy();
        PupilRecordService service = new PupilRecordService(repo, locking);
        service.editPupilDetails("school-user", "P1", "New Name", null, null);
        assertFalse(locking.isLocked("P1"), "Lock must be released once the edit completes");
    }

    @Test
    public void invalidAddressIsRejectedBeforeAcquiringLock() {
        DataRepository repo = newRepositoryWithOnePupil();
        LockingStrategy locking = new InMemoryLockingStrategy();
        PupilRecordService service = new PupilRecordService(repo, locking);
        assertThrows(InvalidAddressException.class,
                () -> service.editPupilDetails("school-user", "P1", null, null, "NA"),
                "A too-short address must raise InvalidAddressException");
        assertFalse(locking.isLocked("P1"), "No lock should have been taken out since validation failed before acquire()");
    }

    @Test
    public void editingUnknownPupilThrowsPupilNotFoundException() {
        DataRepository repo = newRepositoryWithOnePupil();
        PupilRecordService service = new PupilRecordService(repo, new InMemoryLockingStrategy());
        assertThrows(PupilNotFoundException.class,
                () -> service.editPupilDetails("school-user", "DOES-NOT-EXIST", "Name", null, null),
                "Editing a non-existent pupil id must raise PupilNotFoundException");
    }

    @Test
    public void secondUserEditingSameRecordWhileLockedIsRejected() throws Exception {
        DataRepository repo = newRepositoryWithOnePupil();
        LockingStrategy locking = new InMemoryLockingStrategy();
        locking.acquire("P1", "other-user-already-editing");
        PupilRecordService service = new PupilRecordService(repo, locking);
        assertThrows(RecordLockedException.class,
                () -> service.editPupilDetails("school-user", "P1", "New Name", null, null),
                "Editing a record locked by a different user must raise RecordLockedException");
    }
}
