package com.lea.transport.report;

import com.lea.transport.model.BusContractor;
import com.lea.transport.model.Contract;
import com.lea.transport.model.Pupil;
import com.lea.transport.model.School;

import java.util.List;

/** Whole-system summary available only to Administrator. */
public class AdminReport extends Report {
    private final List<School> schools;
    private final List<Pupil> pupils;
    private final List<Contract> contracts;
    private final List<BusContractor> contractors;

    public AdminReport(List<School> schools, List<Pupil> pupils,
                        List<Contract> contracts, List<BusContractor> contractors) {
        this.schools = schools;
        this.pupils = pupils;
        this.contracts = contracts;
        this.contractors = contractors;
    }

    @Override
    public String generate() {
        StringBuilder sb = new StringBuilder(header("ADMIN SYSTEM REPORT"));
        sb.append("Schools registered : ").append(schools.size()).append('\n');
        sb.append("Pupils enrolled    : ").append(pupils.size()).append('\n');
        sb.append("Active contracts   : ").append(contracts.size()).append('\n');
        sb.append("Bus contractors    : ").append(contractors.size()).append('\n');
        long underperforming = contractors.stream().filter(BusContractor::isUnderperforming).count();
        sb.append("Underperforming contractors flagged: ").append(underperforming).append('\n');
        return sb.toString();
    }
}
