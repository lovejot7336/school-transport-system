package com.lea.transport.service;

import com.lea.transport.auth.AccessControl;
import com.lea.transport.auth.SchoolStaff;
import com.lea.transport.exception.AuthorizationException;
import com.lea.transport.report.AdminReport;
import com.lea.transport.report.LEAReport;
import com.lea.transport.report.Report;
import com.lea.transport.report.SchoolReport;
import com.lea.transport.model.School;

import java.util.ArrayList;

/** ITT 3.f: the polymorphic call site for the Report hierarchy. */
public class ReportService {
    private final DataRepository repository;

    public ReportService(DataRepository repository) { this.repository = repository; }

    public Report generateReportFor(AccessControl user) throws AuthorizationException {
        switch (user.getRole()) {
            case ADMIN:
                return new AdminReport(
                        new ArrayList<>(repository.getSchools().values()),
                        new ArrayList<>(repository.getPupils().values()),
                        new ArrayList<>(repository.getContracts().values()),
                        new ArrayList<>(repository.getContractors().values()));
            case SCHOOL: {
                SchoolStaff staff = (SchoolStaff) user;
                School school = repository.getSchool(staff.getSchoolId());
                return new SchoolReport(school, repository.getPupilsForSchool(staff.getSchoolId()));
            }
            case LEA:
                return new LEAReport(
                        new ArrayList<>(repository.getContracts().values()),
                        new ArrayList<>(repository.getContractors().values()));
            default:
                throw new AuthorizationException(user.getRole().name(), "GENERATE_REPORT");
        }
    }
}
