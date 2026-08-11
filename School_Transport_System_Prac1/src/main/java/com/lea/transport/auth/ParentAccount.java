package com.lea.transport.auth;

import java.util.EnumSet;

/** Parents get read-only enquiry access to their own child's route (ITT 3.c). */
public class ParentAccount extends AbstractAccessControl {
    private final String linkedPupilId;

    public ParentAccount(String username, String linkedPupilId) {
        super(username, EnumSet.of(Action.VIEW_BUS_ROUTE));
        this.linkedPupilId = linkedPupilId;
    }

    public String getLinkedPupilId() { return linkedPupilId; }

    @Override
    public Role getRole() { return Role.PARENT; }
}
