package org.example.algorithm;

import org.example.model.City;
import org.example.model.Graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalLong;

/** Exhaustively checks every ordering of the cities other than the selected origin. */
public final class BruteForceTsp {
    public TspResult solve(Graph graph, City origin) {
        Objects.requireNonNull(graph, "graph");
        Objects.requireNonNull(origin, "origin");
        if (!graph.containsCity(origin)) {
            throw new IllegalArgumentException("Origin is not in the graph: " + origin.name());
        }

        if (graph.cities().size() == 1) {
            List<City> route = List.of(origin, origin);
            return new TspResult(route, OptionalLong.of(0),
                    List.of(new RouteEvaluation(route, OptionalLong.of(0))),
                    List.of(new AlgorithmStep(AlgorithmStep.Type.ROUTE_CHECKED, route, OptionalLong.of(0),
                            "A origem é a única cidade; custo total zero")));
        }

        List<City> remaining = new ArrayList<>(graph.cities());
        remaining.remove(origin);
        List<RouteEvaluation> evaluated = new ArrayList<>();
        List<AlgorithmStep> steps = new ArrayList<>();
        SearchBest best = new SearchBest();
        enumerate(graph, origin, origin, remaining, new ArrayList<>(List.of(origin)), 0,
                evaluated, steps, best);
        return new TspResult(best.route, best.cost, evaluated, steps);
    }

    private void enumerate(Graph graph, City origin, City current, List<City> remaining,
                           List<City> path, long cost, List<RouteEvaluation> evaluated,
                           List<AlgorithmStep> steps, SearchBest best) {
        if (remaining.isEmpty()) {
            List<City> closedRoute = new ArrayList<>(path);
            closedRoute.add(origin);
            OptionalLong totalCost = graph.weightBetween(current, origin).isPresent()
                    ? OptionalLong.of(cost + graph.weightBetween(current, origin).getAsInt())
                    : OptionalLong.empty();
            RouteEvaluation candidate = new RouteEvaluation(closedRoute, totalCost);
            evaluated.add(candidate);
            steps.add(new AlgorithmStep(AlgorithmStep.Type.ROUTE_CHECKED, closedRoute,
                    totalCost, totalCost.isPresent()
                    ? "Rota completa avaliada" : "Rota descartada: falta uma conexão"));
            if (totalCost.isPresent() && (!best.cost.isPresent()
                    || totalCost.getAsLong() < best.cost.getAsLong())) {
                best.route = closedRoute;
                best.cost = totalCost;
            }
            return;
        }

        for (int index = 0; index < remaining.size(); index++) {
            City next = remaining.get(index);
            OptionalLong edgeWeight = graph.weightBetween(current, next).isPresent()
                    ? OptionalLong.of(graph.weightBetween(current, next).getAsInt())
                    : OptionalLong.empty();
            if (edgeWeight.isEmpty()) {
                // This ordering cannot be a valid route; still record its completed permutation below.
                List<City> skipped = new ArrayList<>(path);
                skipped.add(next);
                List<City> tail = new ArrayList<>(remaining);
                tail.remove(index);
                appendMissingPermutation(origin, skipped, tail, evaluated, steps);
                continue;
            }
            remaining.remove(index);
            path.add(next);
            enumerate(graph, origin, next, remaining, path, cost + edgeWeight.getAsLong(),
                    evaluated, steps, best);
            path.remove(path.size() - 1);
            remaining.add(index, next);
        }
    }

    private void appendMissingPermutation(City origin, List<City> path, List<City> remaining,
                                         List<RouteEvaluation> evaluated, List<AlgorithmStep> steps) {
        if (remaining.isEmpty()) {
            List<City> closedRoute = new ArrayList<>(path);
            closedRoute.add(origin);
            RouteEvaluation candidate = new RouteEvaluation(closedRoute, OptionalLong.empty());
            evaluated.add(candidate);
            steps.add(new AlgorithmStep(AlgorithmStep.Type.ROUTE_CHECKED, closedRoute, OptionalLong.empty(),
                    "Rota descartada: falta uma conexão"));
            return;
        }
        for (int index = 0; index < remaining.size(); index++) {
            City next = remaining.remove(index);
            path.add(next);
            appendMissingPermutation(origin, path, remaining, evaluated, steps);
            path.remove(path.size() - 1);
            remaining.add(index, next);
        }
    }

    private static final class SearchBest {
        private List<City> route = List.of();
        private OptionalLong cost = OptionalLong.empty();
    }
}
