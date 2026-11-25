package org.example.algorithm;

import org.example.model.City;

import java.util.List;
import java.util.OptionalLong;

/** A didactic event emitted while an algorithm is running. */
public record AlgorithmStep(Type type, List<City> path, OptionalLong cost, String description) {
    public AlgorithmStep {
        path = List.copyOf(path);
    }

    public enum Type {
        ROUTE_CHECKED,
        STATE_SETTLED,
        COST_UPDATED,
        COST_UNCHANGED,
        TOUR_FOUND
    }
}
