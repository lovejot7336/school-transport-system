package com.lea.transport.model;

import java.time.LocalDate;
import java.util.Objects;

/** An awarded ITT contract linking a BusRoute to the BusContractor operating it. */
public class Contract {
    private final String contractId;
    private final BusRoute route;
    private final BusContractor contractor;
    private LocalDate termStart;
    private LocalDate termEnd;

    public Contract(String contractId, BusRoute route, BusContractor contractor,
                     LocalDate termStart, LocalDate termEnd) {
        this.contractId = Objects.requireNonNull(contractId);
        this.route = Objects.requireNonNull(route);
        this.contractor = Objects.requireNonNull(contractor);
        this.termStart = Objects.requireNonNull(termStart);
        this.termEnd = Objects.requireNonNull(termEnd);
        if (termEnd.isBefore(termStart)) throw new IllegalArgumentException("termEnd before termStart");
    }

    public String getContractId() { return contractId; }
    public BusRoute getRoute() { return route; }
    public BusContractor getContractor() { return contractor; }
    public LocalDate getTermStart() { return termStart; }
    public LocalDate getTermEnd() { return termEnd; }

    public void renew(LocalDate newTermEnd) {
        if (newTermEnd.isBefore(this.termEnd)) throw new IllegalArgumentException("must extend contract");
        this.termEnd = newTermEnd;
    }

    @Override
    public String toString() {
        return String.format("Contract %s: %s <-> %s [%s to %s]",
                contractId, route.getRouteId(), contractor.getCompanyName(), termStart, termEnd);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Contract)) return false;
        return contractId.equals(((Contract) o).contractId);
    }

    @Override
    public int hashCode() { return Objects.hash(contractId); }
}
