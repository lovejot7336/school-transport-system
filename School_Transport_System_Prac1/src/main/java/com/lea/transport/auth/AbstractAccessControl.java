package com.lea.transport.auth;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/** Shared plumbing for permitted-action checks; holds no role-specific business behaviour. */
public abstract class AbstractAccessControl implements AccessControl {
    private final String username;
    private final Set<Action> permittedActions;

    protected AbstractAccessControl(String username, Set<Action> permittedActions) {
        this.username = Objects.requireNonNull(username);
        this.permittedActions = permittedActions.isEmpty()
                ? Collections.emptySet() : EnumSet.copyOf(permittedActions);
    }

    @Override
    public String getUsername() { return username; }

    @Override
    public boolean authorize(Action action) { return permittedActions.contains(action); }
}
