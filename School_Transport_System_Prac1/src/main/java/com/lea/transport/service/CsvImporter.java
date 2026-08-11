package com.lea.transport.service;

import com.lea.transport.exception.ImportFailedException;
import com.lea.transport.exception.InvalidAddressException;
import com.lea.transport.model.ParentContact;
import com.lea.transport.model.Pupil;
import com.lea.transport.model.School;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Imports pupil/parent CSV data (ITT requirement 1). Row format:
 * pupilId,name,yearGroup,homeAddress,schoolId,guardianId,guardianName,guardianEmail,guardianPhone
 *
 * Demonstrates both exception categories required by the brief: a
 * checked API exception (IOException from the file API, and
 * NumberFormatException from parsing) and custom generated exceptions
 * (InvalidAddressException, ImportFailedException).
 */
public class CsvImporter {
    private final DataRepository repository;

    public CsvImporter(DataRepository repository) { this.repository = repository; }

    public ImportResult importPupils(Path csvPath) throws ImportFailedException {
        List<String> lines;
        try {
            lines = Files.readAllLines(csvPath);
        } catch (IOException e) {
            throw new ImportFailedException("Could not read CSV file: " + csvPath, e);
        }

        ImportResult result = new ImportResult();
        for (int i = 0; i < lines.size(); i++) {
            int lineNumber = i + 1;
            String line = lines.get(i).trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            try {
                importRow(line);
                result.recordSuccess();
            } catch (InvalidAddressException e) {
                result.recordFailure(lineNumber, e.getMessage());
            } catch (NumberFormatException e) {
                result.recordFailure(lineNumber, "Year group must be numeric: " + e.getMessage());
            } catch (RuntimeException e) {
                result.recordFailure(lineNumber, "Malformed row: " + e.getMessage());
            }
        }

        if (result.getSuccessCount() == 0 && result.hasFailures()) {
            throw new ImportFailedException("Import failed entirely - no valid rows found. "
                    + result.getFailures().size() + " row(s) rejected.");
        }
        return result;
    }

    private void importRow(String line) throws InvalidAddressException {
        String[] cols = line.split(",", -1);
        if (cols.length < 9) throw new IllegalArgumentException("expected 9 columns, found " + cols.length);

        String pupilId = cols[0].trim();
        String name = cols[1].trim();
        int yearGroup = Integer.parseInt(cols[2].trim());
        String homeAddress = cols[3].trim();
        String schoolId = cols[4].trim();
        String guardianId = cols[5].trim();
        String guardianName = cols[6].trim();
        String guardianEmail = cols[7].trim();
        String guardianPhone = cols[8].trim();

        validateAddress(homeAddress);

        School school = repository.getSchool(schoolId);
        if (school == null) throw new IllegalArgumentException("unknown schoolId '" + schoolId + "'");

        ParentContact guardian = new ParentContact(guardianId, guardianName, guardianEmail, guardianPhone);
        Pupil pupil = new Pupil(pupilId, name, yearGroup, homeAddress, guardian);
        pupil.setSchool(school);
        repository.addPupil(pupil);
    }

    static void validateAddress(String address) throws InvalidAddressException {
        if (address == null || address.trim().length() < 5) throw new InvalidAddressException(address);
    }
}
