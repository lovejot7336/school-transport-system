package com.lea.transport.report;

import com.lea.transport.model.Pupil;
import com.lea.transport.model.School;

import java.util.List;

/** Report scoped to a single school's own pupils. */
public class SchoolReport extends Report {
    private final School school;
    private final List<Pupil> pupilsAtSchool;

    public SchoolReport(School school, List<Pupil> pupilsAtSchool) {
        this.school = school;
        this.pupilsAtSchool = pupilsAtSchool;
    }

    @Override
    public String generate() {
        StringBuilder sb = new StringBuilder(header("SCHOOL REPORT: " + school.getName()));
        sb.append("Pupils enrolled at this school: ").append(pupilsAtSchool.size()).append('\n');
        for (Pupil p : pupilsAtSchool) sb.append("  - ").append(p).append('\n');
        return sb.toString();
    }
}
