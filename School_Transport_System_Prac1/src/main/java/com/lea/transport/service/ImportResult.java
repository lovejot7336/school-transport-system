package com.lea.transport.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Outcome of a CSV import: successCount plus a message per failed row - partial success is allowed. */
public class ImportResult {
    private int successCount = 0;
    private final List<String> failures = new ArrayList<>();

    void recordSuccess() { successCount++; }
    void recordFailure(int lineNumber, String reason) { failures.add(String.format("Line %d: %s", lineNumber, reason)); }

    public int getSuccessCount() { return successCount; }
    public List<String> getFailures() { return Collections.unmodifiableList(failures); }
    public boolean hasFailures() { return !failures.isEmpty(); }

    @Override
    public String toString() {
        return String.format("Imported %d row(s) successfully, %d failure(s).", successCount, failures.size());
    }
}
