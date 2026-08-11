package com.lea.transport.model;

import java.util.Objects;

/** A Primary/Junior feeder school or a High School, each with one staff contact. */
public class School {
    private final String schoolId;
    private String name;
    private final SchoolType type;
    private String headTeacherContact;

    public School(String schoolId, String name, SchoolType type, String headTeacherContact) {
        this.schoolId = Objects.requireNonNull(schoolId);
        this.name = Objects.requireNonNull(name);
        this.type = Objects.requireNonNull(type);
        this.headTeacherContact = headTeacherContact;
    }

    public String getSchoolId() { return schoolId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public SchoolType getType() { return type; }
    public String getHeadTeacherContact() { return headTeacherContact; }
    public void setHeadTeacherContact(String c) { this.headTeacherContact = c; }

    @Override
    public String toString() {
        return String.format("%s [%s] (%s) - contact: %s", name, schoolId, type, headTeacherContact);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof School)) return false;
        return schoolId.equals(((School) o).schoolId);
    }

    @Override
    public int hashCode() { return Objects.hash(schoolId); }
}
