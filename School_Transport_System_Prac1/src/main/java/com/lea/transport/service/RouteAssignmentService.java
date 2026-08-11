package com.lea.transport.service;

import com.lea.transport.model.BusRoute;
import com.lea.transport.model.Pupil;

import java.util.Optional;

/**
 * Implements "Reassign Pupil to Bus Route", modelled as an <<extend>>
 * of "Edit Pupil & Parent Details" - only acts when the pupil's address
 * falls outside their current route's catchment.
 */
public class RouteAssignmentService {
    private final DataRepository repository;

    public RouteAssignmentService(DataRepository repository) { this.repository = repository; }

    public Optional<BusRoute> reassignIfCatchmentChanged(Pupil pupil) {
        BusRoute current = pupil.getAssignedRoute();
        if (current != null && current.coversAddress(pupil.getHomeAddress())) {
            return Optional.empty();
        }
        for (BusRoute candidate : repository.getRoutes().values()) {
            if (candidate.coversAddress(pupil.getHomeAddress())) {
                pupil.setAssignedRoute(candidate);
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }
}
