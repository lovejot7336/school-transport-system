package com.lea.transport.model;

import java.util.Objects;

/** A bus company that may tender for and be awarded route contracts. */
public class BusContractor {
    private final String contractorId;
    private String companyName;
    private double performanceRating;

    public BusContractor(String contractorId, String companyName, double performanceRating) {
        this.contractorId = Objects.requireNonNull(contractorId);
        this.companyName = Objects.requireNonNull(companyName);
        setPerformanceRating(performanceRating);
    }

    public String getContractorId() { return contractorId; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String n) { this.companyName = n; }
    public double getPerformanceRating() { return performanceRating; }

    public void setPerformanceRating(double r) {
        if (r < 0.0 || r > 5.0) throw new IllegalArgumentException("performanceRating must be 0.0-5.0");
        this.performanceRating = r;
    }

    public boolean isUnderperforming() { return performanceRating < 2.5; }

    @Override
    public String toString() {
        return String.format("%s [%s] rating=%.1f%s", companyName, contractorId, performanceRating,
                isUnderperforming() ? " (UNDERPERFORMING)" : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BusContractor)) return false;
        return contractorId.equals(((BusContractor) o).contractorId);
    }

    @Override
    public int hashCode() { return Objects.hash(contractorId); }
}
