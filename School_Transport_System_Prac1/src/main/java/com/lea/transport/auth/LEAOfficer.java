package com.lea.transport.auth;

import java.util.EnumSet;

/**
 * LEA can change everything Schools can change, plus change bus
 * contracts and move pupils between routes where they have moved home
 * (ITT 2.c). Also needs VIEW_BUS_ROUTE to check a route before deciding
 * whether reassignment is needed.
 */
public class LEAOfficer extends AbstractAccessControl {
    public LEAOfficer(String username) {
        super(username, EnumSet.of(
                Action.IMPORT_PUPIL_DATA,
                Action.EDIT_PUPIL_DETAILS,
                Action.MANAGE_BUS_CONTRACT,
                Action.REASSIGN_PUPIL_ROUTE,
                Action.VIEW_BUS_ROUTE,
                Action.GENERATE_REPORT));
    }

    @Override
    public Role getRole() { return Role.LEA; }
}
