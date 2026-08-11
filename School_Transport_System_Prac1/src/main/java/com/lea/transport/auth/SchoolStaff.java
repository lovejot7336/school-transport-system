package com.lea.transport.auth;

import java.util.EnumSet;

/**
 * Schools may change pupil/parent information for their own school
 * (ITT 2.b), import the September CSV data, and view routes (needed to
 * confirm a pupil's assignment when editing their details).
 */
public class SchoolStaff extends AbstractAccessControl {
    private final String schoolId;

    public SchoolStaff(String username, String schoolId) {
        super(username, EnumSet.of(
                Action.IMPORT_PUPIL_DATA,
                Action.EDIT_PUPIL_DETAILS,
                Action.VIEW_BUS_ROUTE,
                Action.GENERATE_REPORT));
        this.schoolId = schoolId;
    }

    public String getSchoolId() { return schoolId; }

    @Override
    public Role getRole() { return Role.SCHOOL; }
}
