package com.lea.transport.report;

import com.lea.transport.model.BusContractor;
import com.lea.transport.model.Contract;

import java.util.List;

/** Contract and contractor-performance report for the LEA's annual review. */
public class LEAReport extends Report {
    private final List<Contract> contracts;
    private final List<BusContractor> contractors;

    public LEAReport(List<Contract> contracts, List<BusContractor> contractors) {
        this.contracts = contracts;
        this.contractors = contractors;
    }

    @Override
    public String generate() {
        StringBuilder sb = new StringBuilder(header("LEA CONTRACT & PERFORMANCE REPORT"));
        sb.append("-- Contracts --\n");
        for (Contract c : contracts) sb.append("  - ").append(c).append('\n');
        sb.append("-- Contractor performance --\n");
        for (BusContractor bc : contractors) sb.append("  - ").append(bc).append('\n');
        return sb.toString();
    }
}
