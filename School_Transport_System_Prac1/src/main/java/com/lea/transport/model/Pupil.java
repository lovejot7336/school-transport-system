package com.lea.transport.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A pupil enrolled at a feeder or high school. Every pupil must have at
 * least one guardian - enforced here on the class itself, as a core
 * domain rule rather than a service-layer concern.
 */
public class Pupil {
    private final String pupilId;
    private String name;
    private int yearGroup;
    private String homeAddress;
    private final List<ParentContact> guardians = new ArrayList<>();
    private School school;
    private BusRoute assignedRoute;

    public Pupil(String pupilId, String name, int yearGroup, String homeAddress, ParentContact primaryGuardian) {
        this.pupilId = Objects.requireNonNull(pupilId);
        this.name = Objects.requireNonNull(name);
        this.yearGroup = yearGroup;
        this.homeAddress = Objects.requireNonNull(homeAddress);
        Objects.requireNonNull(primaryGuardian, "A pupil must be enrolled with at least one guardian");
        this.guardians.add(primaryGuardian);
    }

    public String getPupilId() { return pupilId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getYearGroup() { return yearGroup; }
    public void setYearGroup(int yearGroup) { this.yearGroup = yearGroup; }
    public String getHomeAddress() { return homeAddress; }
    public void setHomeAddress(String a) { this.homeAddress = Objects.requireNonNull(a); }
    public List<ParentContact> getGuardians() { return Collections.unmodifiableList(guardians); }
    public void addGuardian(ParentContact c) { guardians.add(Objects.requireNonNull(c)); }
    public School getSchool() { return school; }
    public void setSchool(School school) { this.school = school; }
    public BusRoute getAssignedRoute() { return assignedRoute; }
    public void setAssignedRoute(BusRoute r) { this.assignedRoute = r; }

    @Override
    public String toString() {
        String routeId = assignedRoute == null ? "none" : assignedRoute.getRouteId();
        return String.format("%s (%s) - Year %d, address: %s, route: %s",
                name, pupilId, yearGroup, homeAddress, routeId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pupil)) return false;
        return pupilId.equals(((Pupil) o).pupilId);
    }

    @Override
    public int hashCode() { return Objects.hash(pupilId); }
}
