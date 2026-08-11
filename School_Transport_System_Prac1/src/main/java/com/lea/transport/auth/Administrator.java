package com.lea.transport.auth;

import java.util.EnumSet;

/** ADMIN has access to the whole system, to correct any data (ITT 2.a). */
public class Administrator extends AbstractAccessControl {
    public Administrator(String username) {
        super(username, EnumSet.allOf(Action.class));
    }

    @Override
    public Role getRole() { return Role.ADMIN; }
}
