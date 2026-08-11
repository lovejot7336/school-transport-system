package com.lea.transport.test.suite;

import com.lea.transport.auth.*;
import com.lea.transport.exception.AuthenticationException;
import com.lea.transport.pattern.factory.AccessControlFactory;
import com.lea.transport.service.UserAccountService;
import com.lea.transport.test.Test;

import static com.lea.transport.test.Assertions.*;

/** Covers Role/AccessControl subtype polymorphism, the Factory Method, and authentication. */
public class AuthorizationTests {

    @Test
    public void administratorIsAuthorizedForEveryAction() {
        AccessControl admin = new Administrator("admin");
        for (Action action : Action.values()) {
            assertTrue(admin.authorize(action), "Administrator should be authorised for " + action);
        }
    }

    @Test
    public void parentIsOnlyAuthorizedToViewRoute() {
        AccessControl parent = new ParentAccount("sarah.jenkins", "P1");
        assertTrue(parent.authorize(Action.VIEW_BUS_ROUTE), "Parent should be able to view route");
        assertFalse(parent.authorize(Action.EDIT_PUPIL_DETAILS), "Parent should NOT be able to edit pupil details");
        assertFalse(parent.authorize(Action.MANAGE_BUS_CONTRACT), "Parent should NOT be able to manage contracts");
    }

    @Test
    public void leaOfficerHasSchoolPermissionsPlusContractAndReassign() {
        AccessControl lea = new LEAOfficer("lea.officer");
        assertTrue(lea.authorize(Action.EDIT_PUPIL_DETAILS), "LEA should inherit school-level edit rights");
        assertTrue(lea.authorize(Action.MANAGE_BUS_CONTRACT), "LEA should manage contracts");
        assertTrue(lea.authorize(Action.REASSIGN_PUPIL_ROUTE), "LEA should reassign routes");
        assertTrue(lea.authorize(Action.VIEW_BUS_ROUTE), "LEA should be able to view routes");
        assertFalse(lea.authorize(Action.CORRECT_ANY_DATA), "LEA should NOT have Admin's whole-system correction right");
    }

    @Test
    public void accessControlFactoryProducesCorrectConcreteType() {
        UserAccount adminAccount = new UserAccount("admin", "pw", Role.ADMIN, null, null);
        AccessControl created = AccessControlFactory.create(adminAccount);
        assertTrue(created instanceof Administrator, "Factory should produce an Administrator for ADMIN role");
        assertEquals(Role.ADMIN, created.getRole(), "Created AccessControl should report ADMIN role");

        UserAccount parentAccount = new UserAccount("sarah", "pw", Role.PARENT, null, "P1");
        AccessControl createdParent = AccessControlFactory.create(parentAccount);
        assertTrue(createdParent instanceof ParentAccount, "Factory should produce a ParentAccount for PARENT role");
    }

    @Test
    public void authenticationSucceedsWithCorrectCredentials() throws Exception {
        UserAccountService service = new UserAccountService();
        service.addAccount(new UserAccount("admin", "admin123", Role.ADMIN, null, null));
        UserAccount result = service.authenticate("admin", "admin123");
        assertEquals("admin", result.getUsername(), "Authenticated username should match");
    }

    @Test
    public void authenticationFailsWithWrongPassword() {
        UserAccountService service = new UserAccountService();
        service.addAccount(new UserAccount("admin", "admin123", Role.ADMIN, null, null));
        assertThrows(AuthenticationException.class,
                () -> service.authenticate("admin", "wrong-password"),
                "Wrong password should raise AuthenticationException");
    }

    @Test
    public void authenticationFailsForUnknownUsername() {
        UserAccountService service = new UserAccountService();
        assertThrows(AuthenticationException.class,
                () -> service.authenticate("nobody", "whatever"),
                "Unknown username should raise AuthenticationException");
    }
}
