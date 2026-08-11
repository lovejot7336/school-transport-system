package com.lea.transport.pattern.factory;

import com.lea.transport.auth.*;

/**
 * Factory Method pattern (creational). Callers ask for "the AccessControl
 * for this authenticated account" without needing to know which of the
 * four concrete role classes gets constructed, or with what constructor
 * arguments each one needs. Adding a fifth role in future only requires
 * a new case here, not changes scattered across call sites.
 */
public final class AccessControlFactory {
    private AccessControlFactory() {}

    public static AccessControl create(UserAccount account) {
        switch (account.getRole()) {
            case ADMIN: return new Administrator(account.getUsername());
            case SCHOOL: return new SchoolStaff(account.getUsername(), account.getSchoolId());
            case LEA: return new LEAOfficer(account.getUsername());
            case PARENT: return new ParentAccount(account.getUsername(), account.getLinkedPupilId());
            default: throw new IllegalStateException("Unhandled role: " + account.getRole());
        }
    }
}
