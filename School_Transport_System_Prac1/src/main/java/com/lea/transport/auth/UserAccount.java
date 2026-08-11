package com.lea.transport.auth;

import java.util.Objects;

/**
 * A stored username/password/role record. Passwords are plain text only
 * because this is a hard-coded, in-memory prototype per the brief - a
 * real deployment would hash and salt these.
 */
public class UserAccount {
    private final String username;
    private String password;
    private final Role role;
    private final String schoolId;
    private final String linkedPupilId;

    public UserAccount(String username, String password, Role role, String schoolId, String linkedPupilId) {
        this.username = Objects.requireNonNull(username);
        this.password = Objects.requireNonNull(password);
        this.role = Objects.requireNonNull(role);
        this.schoolId = schoolId;
        this.linkedPupilId = linkedPupilId;
    }

    public String getUsername() { return username; }
    public boolean checkPassword(String candidate) { return password.equals(candidate); }
    public void setPassword(String p) { this.password = Objects.requireNonNull(p); }
    public Role getRole() { return role; }
    public String getSchoolId() { return schoolId; }
    public String getLinkedPupilId() { return linkedPupilId; }
}
