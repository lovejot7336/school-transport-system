package com.lea.transport.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** A bus route running collection points to a destination (High) School. */
public class BusRoute {
    private final String routeId;
    private final List<String> collectionPoints = new ArrayList<>();
    private School destinationSchool;
    private int capacity;

    public BusRoute(String routeId, School destinationSchool, int capacity) {
        this.routeId = Objects.requireNonNull(routeId);
        this.destinationSchool = Objects.requireNonNull(destinationSchool);
        this.capacity = capacity;
    }

    public String getRouteId() { return routeId; }
    public List<String> getCollectionPoints() { return Collections.unmodifiableList(collectionPoints); }
    public void addCollectionPoint(String point) { collectionPoints.add(Objects.requireNonNull(point)); }
    public School getDestinationSchool() { return destinationSchool; }
    public void setDestinationSchool(School s) { this.destinationSchool = s; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int c) { this.capacity = c; }

    /**
     * Coarse catchment check: a collection point is treated as matching
     * if the address contains the point's text (case-insensitive) - a
     * transparent placeholder for real geocoding.
     */
    public boolean coversAddress(String address) {
        if (address == null) return false;
        String lower = address.toLowerCase();
        for (String point : collectionPoints) {
            if (lower.contains(point.toLowerCase())) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("Route %s -> %s [%d stops, capacity %d]",
                routeId, destinationSchool.getName(), collectionPoints.size(), capacity);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BusRoute)) return false;
        return routeId.equals(((BusRoute) o).routeId);
    }

    @Override
    public int hashCode() { return Objects.hash(routeId); }
}
