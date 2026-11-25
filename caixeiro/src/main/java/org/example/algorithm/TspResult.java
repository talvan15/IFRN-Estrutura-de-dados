package org.example.algorithm;

import org.example.model.City;

import java.util.List;
import java.util.OptionalLong;

/** Best tour, if one exists, plus the events and candidates produced by a search. */
public record TspResult(
        List<City> bestRoute,
        OptionalLong bestCost,
        List<RouteEvaluation> evaluatedRoutes,
        List<AlgorithmStep> steps) {
    public TspResult {
        bestRoute = List.copyOf(bestRoute);
        evaluatedRoutes = List.copyOf(evaluatedRoutes);
        steps = List.copyOf(steps);
        if (bestCost.isPresent() != !bestRoute.isEmpty()) {
            throw new IllegalArgumentException("A best route and its cost must either both exist or both be absent");
        }
    }

    public boolean hasTour() {
        return bestCost.isPresent();
    }
}
