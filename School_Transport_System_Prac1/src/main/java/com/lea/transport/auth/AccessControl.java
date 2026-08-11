package com.lea.transport.auth;

/**
 * Behaviour contract shared by every authenticated role. The Facade
 * calls authorize(Action) through this interface without ever needing to
 * know which concrete role class it is talking to - subtype polymorphism
 * in place of branching on role type.
 *
 * Deliberately an interface, not a shared abstract "User" superclass:
 * Administrator and ParentAccount share no meaningful state, only the
 * capability to be authorised, so an interface expresses that more
 * honestly than forcing an inheritance hierarchy between them.
 */
public interface AccessControl {
    String getUsername();
    Role getRole();
    boolean authorize(Action action);
}
