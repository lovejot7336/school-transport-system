package com.lea.transport.test.suite;

import com.lea.transport.exception.ImportFailedException;
import com.lea.transport.model.School;
import com.lea.transport.model.SchoolType;
import com.lea.transport.service.CsvImporter;
import com.lea.transport.service.DataRepository;
import com.lea.transport.service.ImportResult;
import com.lea.transport.test.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static com.lea.transport.test.Assertions.*;

/** Covers CSV import - ITT requirement 1, including the checked-exception (IOException) path. */
public class CsvImporterTests {

    private DataRepository repositoryWithOneSchool() {
        DataRepository repo = new DataRepository();
        repo.addSchool(new School("HS1", "Oakfield High", SchoolType.HIGH, "Mrs Carter"));
        return repo;
    }

    @Test
    public void importsAllValidRowsSuccessfully() throws Exception {
        DataRepository repo = repositoryWithOneSchool();
        Path csv = Files.createTempFile("pupils", ".csv");
        Files.writeString(csv, String.join("\n",
                "P1,Tom Jenkins,11,12 Elm Street,HS1,G1,Sarah Jenkins,sarah@example.com,07700111222",
                "P2,Ama Osei,12,5 Riverside Walk,HS1,G2,Michael Osei,michael@example.com,07700333444"
        ));
        try {
            CsvImporter importer = new CsvImporter(repo);
            ImportResult result = importer.importPupils(csv);
            assertEquals(2, result.getSuccessCount(), "Both valid rows should import successfully");
            assertFalse(result.hasFailures(), "No rows should have failed");
            assertNotNull(repo.getPupil("P1"), "Pupil P1 should now exist in the repository");
            assertNotNull(repo.getPupil("P2"), "Pupil P2 should now exist in the repository");
        } finally {
            Files.deleteIfExists(csv);
        }
    }

    @Test
    public void invalidAddressRowIsRejectedButOtherRowsStillImport() throws Exception {
        DataRepository repo = repositoryWithOneSchool();
        Path csv = Files.createTempFile("pupils", ".csv");
        Files.writeString(csv, String.join("\n",
                "P1,Tom Jenkins,11,12 Elm Street,HS1,G1,Sarah Jenkins,sarah@example.com,07700111222",
                "P2,Bad Row,11,NA,HS1,G2,Michael Osei,michael@example.com,07700333444"
        ));
        try {
            CsvImporter importer = new CsvImporter(repo);
            ImportResult result = importer.importPupils(csv);
            assertEquals(1, result.getSuccessCount(), "Only the valid row should import");
            assertTrue(result.hasFailures(), "The invalid-address row should be recorded as a failure");
            assertNotNull(repo.getPupil("P1"), "The valid pupil should still have been imported");
            assertNull(repo.getPupil("P2"), "The invalid-address pupil should NOT have been imported");
        } finally {
            Files.deleteIfExists(csv);
        }
    }

    @Test
    public void missingFileRaisesImportFailedExceptionWrappingIOException() {
        DataRepository repo = repositoryWithOneSchool();
        CsvImporter importer = new CsvImporter(repo);
        Path missing = Path.of("this/path/definitely/does/not/exist.csv");
        ImportFailedException ex = assertThrows(ImportFailedException.class,
                () -> importer.importPupils(missing),
                "Importing a non-existent file must raise ImportFailedException");
        assertNotNull(ex.getCause(), "The wrapped IOException should be preserved as the cause");
    }

    @Test
    public void fileWithOnlyInvalidRowsRaisesImportFailedException() throws Exception {
        DataRepository repo = repositoryWithOneSchool();
        Path csv = Files.createTempFile("pupils", ".csv");
        Files.writeString(csv, "P1,Bad Row,11,NA,HS1,G1,Sarah Jenkins,sarah@example.com,07700111222");
        try {
            CsvImporter importer = new CsvImporter(repo);
            assertThrows(ImportFailedException.class,
                    () -> importer.importPupils(csv),
                    "A file with zero valid rows should raise ImportFailedException");
        } finally {
            Files.deleteIfExists(csv);
        }
    }
}
