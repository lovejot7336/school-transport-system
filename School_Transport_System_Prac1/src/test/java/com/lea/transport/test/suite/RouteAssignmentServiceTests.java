package com.lea.transport.test.suite;

import com.lea.transport.model.*;
import com.lea.transport.service.DataRepository;
import com.lea.transport.service.RouteAssignmentService;
import com.lea.transport.test.Test;

import java.util.Optional;

import static com.lea.transport.test.Assertions.*;

/** Covers "Reassign Pupil to Bus Route «extend» Edit Pupil & Parent Details". */
public class RouteAssignmentServiceTests {

    private DataRepository repositoryWithTwoRoutes() {
        DataRepository repo = new DataRepository();
        School oakfield = new School("HS1", "Oakfield High", SchoolType.HIGH, "Mrs Carter");
        School riverside = new School("HS2", "Riverside High", SchoolType.HIGH, "Mr Okafor");
        repo.addSchool(oakfield);
        repo.addSchool(riverside);
        BusRoute route1 = new BusRoute("R1", oakfield, 55);
        route1.addCollectionPoint("Elm Street");
        BusRoute route2 = new BusRoute("R2", riverside, 55);
        route2.addCollectionPoint("Mill Lane");
        repo.addRoute(route1);
        repo.addRoute(route2);
        return repo;
    }

    @Test
    public void noReassignmentWhenAddressStillInCurrentCatchment() {
        DataRepository repo = repositoryWithTwoRoutes();
        RouteAssignmentService service = new RouteAssignmentService(repo);
        ParentContact g = new ParentContact("G1", "Guardian", "g@example.com", "0770000000");
        Pupil pupil = new Pupil("P1", "Tom", 11, "12 Elm Street", g);
        pupil.setAssignedRoute(repo.getRoute("R1"));
        Optional<BusRoute> result = service.reassignIfCatchmentChanged(pupil);
        assertTrue(result.isEmpty(), "No reassignment should occur when the address is still covered");
        assertEquals("R1", pupil.getAssignedRoute().getRouteId(), "Pupil should remain on their original route");
    }

    @Test
    public void reassignsToNewRouteWhenAddressMovesOutOfCatchment() {
        DataRepository repo = repositoryWithTwoRoutes();
        RouteAssignmentService service = new RouteAssignmentService(repo);
        ParentContact g = new ParentContact("G1", "Guardian", "g@example.com", "0770000000");
        Pupil pupil = new Pupil("P1", "Tom", 11, "12 Elm Street", g);
        pupil.setAssignedRoute(repo.getRoute("R1"));
        pupil.setHomeAddress("7 Mill Lane");
        Optional<BusRoute> result = service.reassignIfCatchmentChanged(pupil);
        assertTrue(result.isPresent(), "A covering route was found, so a reassignment should occur");
        assertEquals("R2", result.get().getRouteId(), "Pupil should be reassigned to the route covering the new address");
        assertEquals("R2", pupil.getAssignedRoute().getRouteId(), "Pupil's assignedRoute field should reflect the reassignment");
    }

    @Test
    public void noRouteFoundLeavesPupilUnassignedRatherThanGuessing() {
        DataRepository repo = repositoryWithTwoRoutes();
        RouteAssignmentService service = new RouteAssignmentService(repo);
        ParentContact g = new ParentContact("G1", "Guardian", "g@example.com", "0770000000");
        Pupil pupil = new Pupil("P1", "Tom", 11, "12 Elm Street", g);
        pupil.setAssignedRoute(repo.getRoute("R1"));
        pupil.setHomeAddress("1 Somewhere Nobody Covers Avenue");
        Optional<BusRoute> result = service.reassignIfCatchmentChanged(pupil);
        assertTrue(result.isEmpty(), "No covering route exists, so the service must not guess one");
        assertEquals("R1", pupil.getAssignedRoute().getRouteId(),
                "Pupil should remain on their last known route pending manual LEA review");
    }
}
