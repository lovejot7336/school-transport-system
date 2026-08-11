package com.lea.transport.service;

import com.lea.transport.model.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/** Single in-memory store for all domain entities, standing in for a persistence layer. */
public class DataRepository {
    private final Map<String, School> schools = new LinkedHashMap<>();
    private final Map<String, Pupil> pupils = new LinkedHashMap<>();
    private final Map<String, BusRoute> routes = new LinkedHashMap<>();
    private final Map<String, BusContractor> contractors = new LinkedHashMap<>();
    private final Map<String, Contract> contracts = new LinkedHashMap<>();

    public void addSchool(School school) { schools.put(school.getSchoolId(), school); }
    public School getSchool(String id) { return schools.get(id); }
    public Map<String, School> getSchools() { return schools; }

    public void addPupil(Pupil pupil) { pupils.put(pupil.getPupilId(), pupil); }
    public Pupil getPupil(String id) { return pupils.get(id); }
    public Map<String, Pupil> getPupils() { return pupils; }

    public java.util.List<Pupil> getPupilsForSchool(String schoolId) {
        return pupils.values().stream()
                .filter(p -> p.getSchool() != null && p.getSchool().getSchoolId().equals(schoolId))
                .collect(Collectors.toList());
    }

    public void addRoute(BusRoute route) { routes.put(route.getRouteId(), route); }
    public BusRoute getRoute(String id) { return routes.get(id); }
    public Map<String, BusRoute> getRoutes() { return routes; }

    public void addContractor(BusContractor c) { contractors.put(c.getContractorId(), c); }
    public Map<String, BusContractor> getContractors() { return contractors; }

    public void addContract(Contract c) { contracts.put(c.getContractId(), c); }
    public Map<String, Contract> getContracts() { return contracts; }
}
