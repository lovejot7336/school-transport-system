package com.lea.transport.ui;

import com.lea.transport.auth.Role;
import com.lea.transport.auth.UserAccount;
import com.lea.transport.model.*;
import com.lea.transport.service.DataRepository;
import com.lea.transport.service.UserAccountService;

import java.time.LocalDate;

/** Populates the in-memory repository/accounts with demo data (data may be hard-coded per the brief). */
public final class SeedData {
    private SeedData() {}

    public static void populate(DataRepository repo, UserAccountService accounts) {
        School oakfield = new School("HS1", "Oakfield High School", SchoolType.HIGH, "Mrs. J. Carter");
        School riverside = new School("HS2", "Riverside High School", SchoolType.HIGH, "Mr. T. Okafor");
        School millbrook = new School("HS3", "Millbrook High School", SchoolType.HIGH, "Dr. A. Singh");
        School greenacre = new School("PS1", "Greenacre Primary", SchoolType.PRIMARY, "Ms. L. Evans");
        School stMarys = new School("PS2", "St Mary's Junior", SchoolType.PRIMARY, "Mr. D. Patel");
        for (School s : new School[]{oakfield, riverside, millbrook, greenacre, stMarys}) repo.addSchool(s);

        BusRoute route1 = new BusRoute("R1", oakfield, 55);
        route1.addCollectionPoint("Elm Street");
        route1.addCollectionPoint("Greenacre Road");
        BusRoute route2 = new BusRoute("R2", riverside, 55);
        route2.addCollectionPoint("Riverside Walk");
        route2.addCollectionPoint("Mill Lane");
        repo.addRoute(route1);
        repo.addRoute(route2);

        BusContractor goldline = new BusContractor("BC1", "Goldline Coaches", 4.2);
        BusContractor valley = new BusContractor("BC2", "Valley Transport", 1.8);
        repo.addContractor(goldline);
        repo.addContractor(valley);
        repo.addContract(new Contract("CT1", route1, goldline, LocalDate.of(2026, 9, 1), LocalDate.of(2027, 7, 20)));
        repo.addContract(new Contract("CT2", route2, valley, LocalDate.of(2026, 9, 1), LocalDate.of(2027, 7, 20)));

        ParentContact g1 = new ParentContact("G1", "Sarah Jenkins", "sarah.jenkins@example.com", "07700111222");
        Pupil p1 = new Pupil("P1", "Tom Jenkins", 11, "12 Elm Street", g1);
        p1.setSchool(oakfield);
        p1.setAssignedRoute(route1);
        repo.addPupil(p1);

        ParentContact g2 = new ParentContact("G2", "Michael Osei", "michael.osei@example.com", "07700333444");
        Pupil p2 = new Pupil("P2", "Ama Osei", 12, "5 Riverside Walk", g2);
        p2.setSchool(riverside);
        p2.setAssignedRoute(route2);
        repo.addPupil(p2);

        accounts.addAccount(new UserAccount("admin", "admin123", Role.ADMIN, null, null));
        accounts.addAccount(new UserAccount("oakfield.staff", "school123", Role.SCHOOL, "HS1", null));
        accounts.addAccount(new UserAccount("lea.officer", "lea123", Role.LEA, null, null));
        accounts.addAccount(new UserAccount("sarah.jenkins", "parent123", Role.PARENT, null, "P1"));
    }
}
