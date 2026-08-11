package com.lea.transport.ui;

import com.lea.transport.auth.AccessControl;
import com.lea.transport.auth.Role;
import com.lea.transport.exception.*;
import com.lea.transport.model.BusContractor;
import com.lea.transport.model.BusRoute;
import com.lea.transport.pattern.facade.TransportSystemFacade;
import com.lea.transport.pattern.strategy.InMemoryLockingStrategy;
import com.lea.transport.report.Report;
import com.lea.transport.service.DataRepository;
import com.lea.transport.service.ImportResult;
import com.lea.transport.service.UserAccountService;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Console-based menu system (ITT requirement 4.d) exposing every piece of
 * functionality required by the brief, gated by role via
 * TransportSystemFacade. The UI layer's only job is I/O and translating
 * caught exceptions into readable messages - all business logic lives
 * behind the facade.
 */
public class Main {

    public static void main(String[] args) {
        DataRepository repository = new DataRepository();
        UserAccountService accountService = new UserAccountService();
        SeedData.populate(repository, accountService);

        TransportSystemFacade facade = new TransportSystemFacade(
                repository, accountService, new InMemoryLockingStrategy());

        Scanner scanner = new Scanner(System.in);
        System.out.println("=================================================");
        System.out.println(" School Transport System - LEA Console Prototype ");
        System.out.println("=================================================");
        printDemoAccounts();

        AccessControl session = login(scanner, facade);
        if (session == null) {
            System.out.println("Too many failed login attempts. Exiting.");
            return;
        }

        System.out.println("\nWelcome, " + session.getUsername() + " (role: " + session.getRole() + ")");
        boolean running = true;
        while (running) {
            printMenu(session.getRole());
            String choice = prompt(scanner, "Choose an option: ");
            try {
                running = handleChoice(choice, scanner, facade, session);
            } catch (TransportSystemException e) {
                System.out.println("[ERROR] " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Please enter a valid number.");
            }
        }
        System.out.println("Goodbye.");
    }

    private static void printDemoAccounts() {
        System.out.println("Demo accounts:");
        System.out.println("  admin / admin123            (Administrator)");
        System.out.println("  oakfield.staff / school123   (School Staff - Oakfield High)");
        System.out.println("  lea.officer / lea123          (LEA Officer)");
        System.out.println("  sarah.jenkins / parent123     (Parent, linked to pupil P1)");
    }

    private static AccessControl login(Scanner scanner, TransportSystemFacade facade) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            String username = prompt(scanner, "\nUsername: ");
            String password = prompt(scanner, "Password: ");
            try {
                return facade.login(username, password);
            } catch (AuthenticationException e) {
                System.out.println("[ERROR] " + e.getMessage() + " (attempt " + attempt + " of 3)");
            }
        }
        return null;
    }

    private static void printMenu(Role role) {
        System.out.println("\n--- Menu (" + role + ") ---");
        switch (role) {
            case ADMIN:
                System.out.println("1. Import pupil & parent CSV data");
                System.out.println("2. Correct any pupil record");
                System.out.println("3. View a pupil's bus route");
                System.out.println("4. Generate report");
                System.out.println("5. Create a user account");
                System.out.println("0. Exit");
                break;
            case SCHOOL:
                System.out.println("1. Import pupil & parent CSV data");
                System.out.println("2. Edit a pupil's details");
                System.out.println("3. View a pupil's bus route");
                System.out.println("4. Generate report");
                System.out.println("0. Exit");
                break;
            case LEA:
                System.out.println("1. Import pupil & parent CSV data");
                System.out.println("2. Edit a pupil's details");
                System.out.println("3. Reassign a pupil to a new route");
                System.out.println("4. View a pupil's bus route");
                System.out.println("5. Review underperforming contractors");
                System.out.println("6. Generate report");
                System.out.println("0. Exit");
                break;
            case PARENT:
                System.out.println("1. View my child's bus route & collection point");
                System.out.println("0. Exit");
                break;
        }
    }

    private static boolean handleChoice(String choice, Scanner scanner, TransportSystemFacade facade,
                                         AccessControl session) throws TransportSystemException {
        switch (session.getRole()) {
            case ADMIN:
                switch (choice) {
                    case "1": importCsv(scanner, facade, session); return true;
                    case "2": correctPupil(scanner, facade, session); return true;
                    case "3": viewRoute(scanner, facade, session); return true;
                    case "4": generateReport(facade, session); return true;
                    case "5": createAccount(scanner, facade, session); return true;
                    case "0": return false;
                    default: System.out.println("Unknown option."); return true;
                }
            case SCHOOL:
                switch (choice) {
                    case "1": importCsv(scanner, facade, session); return true;
                    case "2": editPupil(scanner, facade, session); return true;
                    case "3": viewRoute(scanner, facade, session); return true;
                    case "4": generateReport(facade, session); return true;
                    case "0": return false;
                    default: System.out.println("Unknown option."); return true;
                }
            case LEA:
                switch (choice) {
                    case "1": importCsv(scanner, facade, session); return true;
                    case "2": editPupil(scanner, facade, session); return true;
                    case "3": reassignRoute(scanner, facade, session); return true;
                    case "4": viewRoute(scanner, facade, session); return true;
                    case "5": reviewContractors(facade, session); return true;
                    case "6": generateReport(facade, session); return true;
                    case "0": return false;
                    default: System.out.println("Unknown option."); return true;
                }
            case PARENT:
                switch (choice) {
                    case "1": viewOwnRoute(facade, session); return true;
                    case "0": return false;
                    default: System.out.println("Unknown option."); return true;
                }
            default:
                return false;
        }
    }

    private static void importCsv(Scanner scanner, TransportSystemFacade facade, AccessControl session)
            throws TransportSystemException {
        String path = prompt(scanner, "CSV file path (e.g. data/pupils_september.csv): ");
        ImportResult result = facade.importPupilData(session, Path.of(path));
        System.out.println(result);
        if (result.hasFailures()) {
            result.getFailures().forEach(f -> System.out.println("  - " + f));
        }
    }

    private static void editPupil(Scanner scanner, TransportSystemFacade facade, AccessControl session)
            throws TransportSystemException {
        String pupilId = prompt(scanner, "Pupil ID to edit: ");
        String name = prompt(scanner, "New name (blank to keep unchanged): ");
        String yearStr = prompt(scanner, "New year group (blank to keep unchanged): ");
        String address = prompt(scanner, "New home address (blank to keep unchanged): ");

        Integer year = yearStr.isEmpty() ? null : Integer.valueOf(yearStr);
        TransportSystemFacade.EditOutcome outcome = facade.editPupilDetails(
                session, pupilId, name.isEmpty() ? null : name, year, address.isEmpty() ? null : address);

        System.out.println("Pupil record updated.");
        if (outcome.addressChanged) {
            if (outcome.newRoute.isPresent()) {
                System.out.println("Address change moved this pupil into a new catchment - "
                        + "reassigned to " + outcome.newRoute.get());
            } else {
                System.out.println("Address changed, but no covering route was found. Flagging for manual LEA review.");
            }
        }
    }

    private static void correctPupil(Scanner scanner, TransportSystemFacade facade, AccessControl session)
            throws TransportSystemException {
        String pupilId = prompt(scanner, "Pupil ID to correct: ");
        String name = prompt(scanner, "New name (blank to keep unchanged): ");
        String yearStr = prompt(scanner, "New year group (blank to keep unchanged): ");
        String address = prompt(scanner, "New home address (blank to keep unchanged): ");

        Integer year = yearStr.isEmpty() ? null : Integer.valueOf(yearStr);
        TransportSystemFacade.EditOutcome outcome = facade.correctPupilRecord(
                session, pupilId, name.isEmpty() ? null : name, year, address.isEmpty() ? null : address);

        System.out.println("Pupil record corrected by Administrator.");
        if (outcome.addressChanged) {
            System.out.println(outcome.newRoute.isPresent()
                    ? "Reassigned to " + outcome.newRoute.get()
                    : "No covering route found for new address.");
        }
    }

    private static void reassignRoute(Scanner scanner, TransportSystemFacade facade, AccessControl session)
            throws TransportSystemException {
        String pupilId = prompt(scanner, "Pupil ID to reassign: ");
        Optional<BusRoute> newRoute = facade.reassignPupilRoute(session, pupilId);
        System.out.println(newRoute.isPresent()
                ? "Reassigned to " + newRoute.get()
                : "Pupil's current route already covers their address - no change made.");
    }

    private static void viewRoute(Scanner scanner, TransportSystemFacade facade, AccessControl session)
            throws TransportSystemException {
        String pupilId = prompt(scanner, "Pupil ID: ");
        BusRoute route = facade.viewRoute(session, pupilId);
        System.out.println(route == null ? "No route currently assigned." : route);
    }

    private static void viewOwnRoute(TransportSystemFacade facade, AccessControl session)
            throws TransportSystemException {
        com.lea.transport.auth.ParentAccount parent = (com.lea.transport.auth.ParentAccount) session;
        BusRoute route = facade.viewRoute(session, parent.getLinkedPupilId());
        if (route == null) {
            System.out.println("No route currently assigned.");
        } else {
            System.out.println("Route: " + route);
            System.out.println("Collection points: " + route.getCollectionPoints());
        }
    }

    private static void reviewContractors(TransportSystemFacade facade, AccessControl session)
            throws TransportSystemException {
        List<BusContractor> underperforming = facade.listUnderperformingContractors(session);
        if (underperforming.isEmpty()) {
            System.out.println("No underperforming contractors this review cycle.");
        } else {
            System.out.println("Underperforming contractors:");
            underperforming.forEach(c -> System.out.println("  - " + c));
        }
    }

    private static void generateReport(TransportSystemFacade facade, AccessControl session)
            throws TransportSystemException {
        Report report = facade.generateReport(session);
        System.out.println(report.generate());
    }

    private static void createAccount(Scanner scanner, TransportSystemFacade facade, AccessControl session)
            throws TransportSystemException {
        String username = prompt(scanner, "New username: ");
        String password = prompt(scanner, "New password: ");
        String roleStr = prompt(scanner, "Role (ADMIN/SCHOOL/LEA/PARENT): ").toUpperCase();
        Role role = Role.valueOf(roleStr);
        String schoolId = role == Role.SCHOOL ? prompt(scanner, "School ID: ") : null;
        String linkedPupil = role == Role.PARENT ? prompt(scanner, "Linked pupil ID: ") : null;
        facade.createAccount(session, new com.lea.transport.auth.UserAccount(
                username, password, role, schoolId, linkedPupil));
        System.out.println("Account created for " + username + " (" + role + ").");
    }

    private static String prompt(Scanner scanner, String label) {
        System.out.print(label);
        return scanner.nextLine().trim();
    }
}
