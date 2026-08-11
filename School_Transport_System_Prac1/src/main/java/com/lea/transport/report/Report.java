package com.lea.transport.report;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ITT 3.f: reporting facilities for each level of access. Each access
 * level gets its own subclass overriding generate() - one polymorphic
 * call site in ReportService produces the right report regardless of
 * which concrete type is passed in, instead of branching per role.
 */
public abstract class Report {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    protected final LocalDateTime generatedAt;

    protected Report() { this.generatedAt = LocalDateTime.now(); }

    public LocalDateTime getGeneratedAt() { return generatedAt; }

    protected String header(String title) {
        return "=== " + title + " (generated " + generatedAt.format(FORMAT) + ") ===\n";
    }

    public abstract String generate();
}
