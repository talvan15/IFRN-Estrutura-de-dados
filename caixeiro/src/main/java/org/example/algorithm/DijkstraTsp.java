package org.example.algorithm;

import org.example.model.City;
import org.example.model.Graph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.math.BigInteger;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.PriorityQueue;

/**
 * Finds a minimum Hamiltonian cycle by applying Dijkstra to expanded states:
 * (current city, set of cities already visited).
 */
public final class DijkstraTsp {
    public TspResult solve(Graph graph, City origin) {
        Objects.requireNonNull(graph, "graph");
        Objects.requireNonNull(origin, "origin");
        if (!graph.containsCity(origin)) {
            throw new IllegalArgumentException("Origin is not in the graph: " + origin.name());
        }

        List<City> cities = graph.cities();
        if (cities.size() == 1) {
            List<City> route = List.of(origin, origin);
            return new TspResult(route, OptionalLong.of(0),
                    List.of(new RouteEvaluation(route, OptionalLong.of(0))),
                    List.of(new AlgorithmStep(AlgorithmStep.Type.TOUR_FOUND, route, OptionalLong.of(0),
                            "A origem é a única cidade; custo total zero")));
        }

        Map<City, Integer> cityIndex = new HashMap<>();
        for (int i = 0; i < cities.size(); i++) {
            cityIndex.put(cities.get(i), i);
        }
        int originIndex = cityIndex.get(origin);
        BigInteger allVisited = BigInteger.ONE.shiftLeft(cities.size()).subtract(BigInteger.ONE);
        State initial = new State(originIndex, BigInteger.ZERO.setBit(originIndex));

        Map<State, Long> distances = new HashMap<>();
        Map<State, State> predecessors = new HashMap<>();
        PriorityQueue<QueueEntry> queue = new PriorityQueue<>(
                Comparator.comparingLong(QueueEntry::distance)
                        .thenComparingLong(QueueEntry::sequence));
        List<RouteEvaluation> tours = new ArrayList<>();
        List<AlgorithmStep> steps = new ArrayList<>();
        long sequence = 0;
        distances.put(initial, 0L);
        queue.add(new QueueEntry(initial, 0, sequence++));

        OptionalLong bestCost = OptionalLong.empty();
        List<City> bestRoute = List.of();

        while (!queue.isEmpty()) {
            QueueEntry entry = queue.remove();
            State state = entry.state();
            long distance = entry.distance();
            if (distance != distances.getOrDefault(state, Long.MAX_VALUE)) {
                continue;
            }
            if (bestCost.isPresent() && distance > bestCost.getAsLong()) {
                break;
            }

            List<City> currentPath = reconstructPath(state, predecessors, cities);
            steps.add(new AlgorithmStep(AlgorithmStep.Type.STATE_SETTLED, currentPath,
                    OptionalLong.of(distance), "Estado escolhido com menor custo conhecido"));

            if (state.visitedSet().equals(allVisited)) {
                City current = cities.get(state.currentIndex());
                OptionalInt returnWeight = graph.weightBetween(current, origin);
                if (returnWeight.isPresent()) {
                    long tourCost = distance + returnWeight.getAsInt();
                    List<City> route = new ArrayList<>(currentPath);
                    route.add(origin);
                    tours.add(new RouteEvaluation(route, OptionalLong.of(tourCost)));
                    steps.add(new AlgorithmStep(AlgorithmStep.Type.TOUR_FOUND, route, OptionalLong.of(tourCost),
                            "Todas as cidades visitadas; retorno à origem possível"));
                    if (!bestCost.isPresent() || tourCost < bestCost.getAsLong()) {
                        bestCost = OptionalLong.of(tourCost);
                        bestRoute = route;
                    }
                } else {
                    List<City> route = new ArrayList<>(currentPath);
                    route.add(origin);
                    tours.add(new RouteEvaluation(route, OptionalLong.empty()));
                    steps.add(new AlgorithmStep(AlgorithmStep.Type.COST_UNCHANGED, currentPath,
                            OptionalLong.of(distance), "Todas as cidades visitadas, mas falta retorno à origem"));
                }
                continue;
            }

            City current = cities.get(state.currentIndex());
            for (Map.Entry<City, Integer> neighbor : graph.neighborsOf(current).entrySet()) {
                int nextIndex = cityIndex.get(neighbor.getKey());
                if (state.visitedSet().testBit(nextIndex)) {
                    continue;
                }
                long candidateDistance = distance + neighbor.getValue();
                State next = new State(nextIndex, state.visitedSet().setBit(nextIndex));
                long knownDistance = distances.getOrDefault(next, Long.MAX_VALUE);
                if (candidateDistance < knownDistance) {
                    distances.put(next, candidateDistance);
                    predecessors.put(next, state);
                    queue.add(new QueueEntry(next, candidateDistance, sequence++));
                    List<City> candidatePath = new ArrayList<>(currentPath);
                    candidatePath.add(neighbor.getKey());
                    steps.add(new AlgorithmStep(AlgorithmStep.Type.COST_UPDATED, candidatePath,
                            OptionalLong.of(candidateDistance), "Custo atualizado pelo predecessor atual"));
                } else {
                    List<City> candidatePath = new ArrayList<>(currentPath);
                    candidatePath.add(neighbor.getKey());
                    steps.add(new AlgorithmStep(AlgorithmStep.Type.COST_UNCHANGED, candidatePath,
                            OptionalLong.of(candidateDistance), "Custo conhecido para este estado é menor ou igual"));
                }
            }
        }

        return new TspResult(bestRoute, bestCost, tours, steps);
    }

    private List<City> reconstructPath(State state, Map<State, State> predecessors, List<City> cities) {
        List<City> reversed = new ArrayList<>();
        State cursor = state;
        while (cursor != null) {
            reversed.add(cities.get(cursor.currentIndex()));
            cursor = predecessors.get(cursor);
        }
        java.util.Collections.reverse(reversed);
        return reversed;
    }

    private record State(int currentIndex, BigInteger visitedSet) { }

    private record QueueEntry(State state, long distance, long sequence) { }
}
