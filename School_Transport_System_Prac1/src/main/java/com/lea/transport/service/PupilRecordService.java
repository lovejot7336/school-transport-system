package com.lea.transport.service;

import com.lea.transport.exception.InvalidAddressException;
import com.lea.transport.exception.PupilNotFoundException;
import com.lea.transport.exception.RecordLockedException;
import com.lea.transport.model.Pupil;
import com.lea.transport.pattern.strategy.LockingStrategy;

/**
 * Edits pupil/parent records under the locking mechanism required by ITT
 * 3.e. Every edit: validate first (fail fast before touching any lock
 * slot), acquire the lock, mutate, then always release - even if the
 * mutation fails.
 */
public class PupilRecordService {
    private final DataRepository repository;
    private final LockingStrategy lockingStrategy;

    public PupilRecordService(DataRepository repository, LockingStrategy lockingStrategy) {
        this.repository = repository;
        this.lockingStrategy = lockingStrategy;
    }

    /** @return true if the home address actually changed. */
    public boolean editPupilDetails(String actorUsername, String pupilId,
                                     String newName, Integer newYearGroup, String newAddress)
            throws PupilNotFoundException, InvalidAddressException, RecordLockedException {

        Pupil pupil = repository.getPupil(pupilId);
        if (pupil == null) throw new PupilNotFoundException(pupilId);

        boolean addressChanging = newAddress != null && !newAddress.equals(pupil.getHomeAddress());
        if (newAddress != null) {
            CsvImporter.validateAddress(newAddress);
        }

        lockingStrategy.acquire(pupilId, actorUsername);
        try {
            if (newName != null) pupil.setName(newName);
            if (newYearGroup != null) pupil.setYearGroup(newYearGroup);
            if (newAddress != null) pupil.setHomeAddress(newAddress);
        } finally {
            lockingStrategy.release(pupilId, actorUsername);
        }
        return addressChanging;
    }

    public Pupil getPupil(String pupilId) throws PupilNotFoundException {
        Pupil pupil = repository.getPupil(pupilId);
        if (pupil == null) throw new PupilNotFoundException(pupilId);
        return pupil;
    }
}
