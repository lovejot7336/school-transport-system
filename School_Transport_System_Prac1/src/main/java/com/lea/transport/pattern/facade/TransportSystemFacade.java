package com.lea.transport.pattern.facade;

import com.lea.transport.auth.*;
import com.lea.transport.exception.*;
import com.lea.transport.model.BusContractor;
import com.lea.transport.model.BusRoute;
import com.lea.transport.model.Pupil;
import com.lea.transport.pattern.factory.AccessControlFactory;
import com.lea.transport.report.Report;
import com.lea.transport.service.*;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Facade pattern (structural). The console UI talks to this one class
 * only. Also centralises the cross-cutting concern every operation
 * shares: checking AccessControl.authorize(action) before delegating.
 */
public class TransportSystemFacade {
    private final DataRepository repository;
    private final UserAccountService accountService;
    private final CsvImporter csvImporter;
    private final PupilRecordService pupilRecordService;
    private final RouteAssignmentService routeAssignmentService;
    private final ContractService contractService;
    private final ReportService reportService;

    public TransportSystemFacade(DataRepository repository,
                                  UserAccountService accountService,
                                  com.lea.transport.pattern.strategy.LockingStrategy lockingStrategy) {
        this.repository = repository;
        this.accountService = accountService;
        this.csvImporter = new CsvImporter(repository);
        this.pupilRecordService = new PupilRecordService(repository, lockingStrategy);
        this.routeAssignmentService = new RouteAssignmentService(repository);
        this.contractService = new ContractService(repository);
        this.reportService = new ReportService(repository);
    }

    public DataRepository getRepository() { return repository; }

    public AccessControl login(String username, String password) throws AuthenticationException {
        UserAccount account = accountService.authenticate(username, password);
        return AccessControlFactory.create(account);
    }

    public ImportResult importPupilData(AccessControl actor, Path csvPath)
            throws AuthorizationException, ImportFailedException {
        requireAuthorized(actor, Action.IMPORT_PUPIL_DATA);
        return csvImporter.importPupils(csvPath);
    }

    public static class EditOutcome {
        public final boolean addressChanged;
        public final Optional<BusRoute> newRoute;
        EditOutcome(boolean addressChanged, Optional<BusRoute> newRoute) {
            this.addressChanged = addressChanged;
            this.newRoute = newRoute;
        }
    }

    public EditOutcome editPupilDetails(AccessControl actor, String pupilId,
                                         String newName, Integer newYearGroup, String newAddress)
            throws AuthorizationException, PupilNotFoundException, InvalidAddressException, RecordLockedException {
        requireAuthorized(actor, Action.EDIT_PUPIL_DETAILS);
        enforceSchoolScope(actor, pupilId);

        boolean addressChanged = pupilRecordService.editPupilDetails(
                actor.getUsername(), pupilId, newName, newYearGroup, newAddress);

        Optional<BusRoute> newRoute = Optional.empty();
        if (addressChanged) {
            Pupil pupil = repository.getPupil(pupilId);
            newRoute = routeAssignmentService.reassignIfCatchmentChanged(pupil);
        }
        return new EditOutcome(addressChanged, newRoute);
    }

    private void enforceSchoolScope(AccessControl actor, String pupilId) throws AuthorizationException, PupilNotFoundException {
        if (actor.getRole() != Role.SCHOOL) return;
        SchoolStaff staff = (SchoolStaff) actor;
        Pupil pupil = repository.getPupil(pupilId);
        if (pupil == null) throw new PupilNotFoundException(pupilId);
        if (pupil.getSchool() == null || !pupil.getSchool().getSchoolId().equals(staff.getSchoolId())) {
            throw new AuthorizationException(actor.getRole().name(), "EDIT_PUPIL_DETAILS (different school)");
        }
    }

    public Optional<BusRoute> reassignPupilRoute(AccessControl actor, String pupilId)
            throws AuthorizationException, PupilNotFoundException {
        requireAuthorized(actor, Action.REASSIGN_PUPIL_ROUTE);
        Pupil pupil = repository.getPupil(pupilId);
        if (pupil == null) throw new PupilNotFoundException(pupilId);
        return routeAssignmentService.reassignIfCatchmentChanged(pupil);
    }

    public BusRoute viewRoute(AccessControl actor, String pupilId)
            throws AuthorizationException, PupilNotFoundException {
        requireAuthorized(actor, Action.VIEW_BUS_ROUTE);
        if (actor.getRole() == Role.PARENT) {
            ParentAccount parent = (ParentAccount) actor;
            if (!pupilId.equals(parent.getLinkedPupilId())) {
                throw new AuthorizationException(actor.getRole().name(), "VIEW_BUS_ROUTE (different pupil)");
            }
        }
        Pupil pupil = pupilRecordService.getPupil(pupilId);
        return pupil.getAssignedRoute();
    }

    public void addContract(AccessControl actor, com.lea.transport.model.Contract contract) throws AuthorizationException {
        requireAuthorized(actor, Action.MANAGE_BUS_CONTRACT);
        contractService.addContract(contract);
    }

    public List<BusContractor> listUnderperformingContractors(AccessControl actor) throws AuthorizationException {
        requireAuthorized(actor, Action.MANAGE_BUS_CONTRACT);
        return contractService.listUnderperformingContractors();
    }

    public EditOutcome correctPupilRecord(AccessControl actor, String pupilId,
                                           String newName, Integer newYearGroup, String newAddress)
            throws AuthorizationException, PupilNotFoundException, InvalidAddressException, RecordLockedException {
        requireAuthorized(actor, Action.CORRECT_ANY_DATA);
        boolean addressChanged = pupilRecordService.editPupilDetails(
                actor.getUsername(), pupilId, newName, newYearGroup, newAddress);
        Optional<BusRoute> newRoute = Optional.empty();
        if (addressChanged) {
            Pupil pupil = repository.getPupil(pupilId);
            newRoute = routeAssignmentService.reassignIfCatchmentChanged(pupil);
        }
        return new EditOutcome(addressChanged, newRoute);
    }

    public void createAccount(AccessControl actor, UserAccount newAccount) throws AuthorizationException {
        requireAuthorized(actor, Action.MANAGE_USER_ACCOUNTS);
        accountService.addAccount(newAccount);
    }

    public Report generateReport(AccessControl actor) throws AuthorizationException {
        requireAuthorized(actor, Action.GENERATE_REPORT);
        return reportService.generateReportFor(actor);
    }

    private void requireAuthorized(AccessControl actor, Action action) throws AuthorizationException {
        if (!actor.authorize(action)) {
            throw new AuthorizationException(actor.getRole().name(), action.name());
        }
    }
}
