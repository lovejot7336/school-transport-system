package com.lea.transport.test.suite;

import com.lea.transport.auth.AccessControl;
import com.lea.transport.auth.Role;
import com.lea.transport.auth.UserAccount;
import com.lea.transport.exception.AuthorizationException;
import com.lea.transport.model.ParentContact;
import com.lea.transport.model.Pupil;
import com.lea.transport.model.School;
import com.lea.transport.model.SchoolType;
import com.lea.transport.pattern.facade.TransportSystemFacade;
import com.lea.transport.pattern.strategy.InMemoryLockingStrategy;
import com.lea.transport.report.Report;
import com.lea.transport.service.DataRepository;
import com.lea.transport.service.UserAccountService;
import com.lea.transport.test.Test;

import static com.lea.transport.test.Assertions.*;

/** Covers the Facade end-to-end: role/school/pupil scoping, all in one integration point. */
public class FacadeAuthorizationTests {

    private TransportSystemFacade facade;
    private UserAccountService accounts;

    private void setUp() {
        DataRepository repo = new DataRepository();
        accounts = new UserAccountService();

        School oakfield = new School("HS1", "Oakfield High", SchoolType.HIGH, "Mrs Carter");
        School riverside = new School("HS2", "Riverside High", SchoolType.HIGH, "Mr Okafor");
        repo.addSchool(oakfield);
        repo.addSchool(riverside);

        ParentContact g1 = new ParentContact("G1", "Sarah Jenkins", "sarah@example.com", "07700111222");
        Pupil p1 = new Pupil("P1", "Tom Jenkins", 11, "12 Elm Street", g1);
        p1.setSchool(oakfield);
        repo.addPupil(p1);

        ParentContact g2 = new ParentContact("G2", "Michael Osei", "michael@example.com", "07700333444");
        Pupil p2 = new Pupil("P2", "Ama Osei", 12, "5 Riverside Walk", g2);
        p2.setSchool(riverside);
        repo.addPupil(p2);

        accounts.addAccount(new UserAccount("admin", "admin123", Role.ADMIN, null, null));
        accounts.addAccount(new UserAccount("oakfield.staff", "school123", Role.SCHOOL, "HS1", null));
        accounts.addAccount(new UserAccount("lea.officer", "lea123", Role.LEA, null, null));
        accounts.addAccount(new UserAccount("sarah.jenkins", "parent123", Role.PARENT, null, "P1"));

        facade = new TransportSystemFacade(repo, accounts, new InMemoryLockingStrategy());
    }

    @Test
    public void parentCannotEditPupilDetails() throws Exception {
        setUp();
        AccessControl parent = facade.login("sarah.jenkins", "parent123");
        assertThrows(AuthorizationException.class,
                () -> facade.editPupilDetails(parent, "P1", "New Name", null, null),
                "Parent role must not be authorised to edit pupil details");
    }

    @Test
    public void schoolStaffCannotEditPupilAtADifferentSchool() throws Exception {
        setUp();
        AccessControl oakfieldStaff = facade.login("oakfield.staff", "school123");
        assertThrows(AuthorizationException.class,
                () -> facade.editPupilDetails(oakfieldStaff, "P2", "New Name", null, null),
                "School Staff must not be able to edit a pupil from a different school (P2 is Riverside, not Oakfield)");
    }

    @Test
    public void schoolStaffCanEditItsOwnSchoolsPupil() throws Exception {
        setUp();
        AccessControl oakfieldStaff = facade.login("oakfield.staff", "school123");
        TransportSystemFacade.EditOutcome outcome =
                facade.editPupilDetails(oakfieldStaff, "P1", "Thomas Jenkins", null, null);
        assertFalse(outcome.addressChanged, "This edit did not touch the address");
        assertEquals("Thomas Jenkins", facade.getRepository().getPupil("P1").getName(), "Pupil's name should have been updated");
    }

    @Test
    public void parentCannotViewAnotherPupilsRoute() throws Exception {
        setUp();
        AccessControl parent = facade.login("sarah.jenkins", "parent123");
        assertThrows(AuthorizationException.class,
                () -> facade.viewRoute(parent, "P2"),
                "Parent linked to P1 must not be able to view P2's route");
    }

    @Test
    public void adminCanCorrectAnyPupilRegardlessOfSchool() throws Exception {
        setUp();
        AccessControl admin = facade.login("admin", "admin123");
        TransportSystemFacade.EditOutcome outcome =
                facade.correctPupilRecord(admin, "P2", "Ama Osei-Boateng", null, null);
        assertFalse(outcome.addressChanged, "This correction did not touch the address");
        assertEquals("Ama Osei-Boateng", facade.getRepository().getPupil("P2").getName(),
                "Admin should be able to correct a pupil at any school");
    }

    @Test
    public void leaOfficerCannotUseAdminOnlyCorrectAction() throws Exception {
        setUp();
        AccessControl lea = facade.login("lea.officer", "lea123");
        assertThrows(AuthorizationException.class,
                () -> facade.correctPupilRecord(lea, "P1", "Someone Else", null, null),
                "LEA Officer must not be authorised for the Admin-only CORRECT_ANY_DATA action");
    }

    @Test
    public void reportGenerationReturnsRoleAppropriateReportType() throws Exception {
        setUp();
        AccessControl admin = facade.login("admin", "admin123");
        AccessControl oakfieldStaff = facade.login("oakfield.staff", "school123");
        Report adminReport = facade.generateReport(admin);
        Report schoolReport = facade.generateReport(oakfieldStaff);
        assertTrue(adminReport instanceof com.lea.transport.report.AdminReport, "Admin should receive an AdminReport");
        assertTrue(schoolReport instanceof com.lea.transport.report.SchoolReport, "School Staff should receive a SchoolReport");
        assertTrue(adminReport.generate().contains("ADMIN SYSTEM REPORT"), "AdminReport text should be the whole-system report");
    }

    @Test
    public void schoolStaffAndLeaOfficerCanBothViewAPupilsRoute() throws Exception {
        setUp();
        AccessControl oakfieldStaff = facade.login("oakfield.staff", "school123");
        AccessControl lea = facade.login("lea.officer", "lea123");
        facade.viewRoute(oakfieldStaff, "P1");
        facade.viewRoute(lea, "P1");
    }
}
