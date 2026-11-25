package org.example.algorithm;

import org.example.model.City;
import org.example.model.Graph;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Dependency-free checks; run this class with assertions enabled or use its explicit checks. */
public final class AlgorithmChecks {
    private AlgorithmChecks() { }

    public static void main(String[] args) {
        checksGraphConstructionAndWeights();
        checksProfessorFourCityExample();
        checksDisconnectedGraphHasNoTour();
        checksInvalidPermutationsStillVisitEveryCityOnce();
        checksReturnToOriginIsIncluded();
        checksSingleCityHasZeroCost();
        checksDijkstraStateSupportsMoreThanSixtyThreeCities();
        System.out.println("Algorithm checks passed");
    }

    private static void checksGraphConstructionAndWeights() {
        City a = new City("A");
        City b = new City("B");
        City c = new City("C");
        Graph graph = new Graph();
        graph.addCity(a);
        graph.addCity(b);
        graph.addCity(c);
        graph.addEdge(a, b, 7);

        check(graph.cities().size() == 3, "Graph should contain all added cities");
        check(graph.edges().size() == 1, "Graph should retain the added road");
        check(graph.weightBetween(a, b).orElseThrow() == 7, "Weight lookup should return road weight");
        check(graph.weightBetween(b, a).orElseThrow() == 7, "Undirected weight lookup should work both ways");
        check(graph.neighborsOf(a).get(b) == 7, "Adjacency lookup should expose direct weight");
        check(graph.weightBetween(a, c).isEmpty(), "Missing road should have no weight");
    }

    private static void checksProfessorFourCityExample() {
        Graph graph = professorGraph();
        City origin = new City("V1");
        TspResult bruteForce = new BruteForceTsp().solve(graph, origin);
        TspResult dijkstra = new DijkstraTsp().solve(graph, origin);

        check(bruteForce.bestCost().orElseThrow() == 11, "Brute force cost should be 11");
        check(dijkstra.bestCost().orElseThrow() == 11, "Dijkstra TSP cost should be 11");
        check(bruteForce.evaluatedRoutes().size() == 6, "Brute force should evaluate six routes");
        Map<String, Long> expectedCosts = Map.of(
                "V1-V2-V3-V4-V1", 12L,
                "V1-V2-V4-V3-V1", 11L,
                "V1-V3-V2-V4-V1", 15L,
                "V1-V3-V4-V2-V1", 11L,
                "V1-V4-V2-V3-V1", 15L,
                "V1-V4-V3-V2-V1", 12L);
        Map<String, Long> actualCosts = bruteForce.evaluatedRoutes().stream()
                .collect(Collectors.toMap(route -> route.route().stream().map(City::name)
                        .collect(Collectors.joining("-")), route -> route.cost().orElseThrow()));
        check(actualCosts.equals(expectedCosts), "All six route costs should match the professor example");
        check(closedAtOrigin(bruteForce.bestRoute(), origin), "Brute force route must return to origin");
        check(closedAtOrigin(dijkstra.bestRoute(), origin), "Dijkstra route must return to origin");
        check(dijkstra.steps().stream().anyMatch(s -> s.type() == AlgorithmStep.Type.COST_UPDATED),
                "Dijkstra trace should include cost updates");
        check(dijkstra.steps().stream().anyMatch(s -> s.type() == AlgorithmStep.Type.TOUR_FOUND),
                "Dijkstra trace should include completed tours");
    }

    private static void checksDisconnectedGraphHasNoTour() {
        City a = new City("A");
        City b = new City("B");
        City c = new City("C");
        Graph graph = new Graph();
        graph.addCity(a);
        graph.addCity(b);
        graph.addCity(c);
        graph.addEdge(a, b, 2);
        graph.addEdge(b, c, 3);

        check(!new BruteForceTsp().solve(graph, a).hasTour(), "Brute force must reject missing roads");
        check(!new DijkstraTsp().solve(graph, a).hasTour(), "Dijkstra TSP must report no cycle");
    }

    private static void checksInvalidPermutationsStillVisitEveryCityOnce() {
        Graph graph = new Graph();
        List<City> cities = List.of(new City("A"), new City("B"), new City("C"), new City("D"));
        cities.forEach(graph::addCity);
        City origin = cities.getFirst();
        TspResult result = new BruteForceTsp().solve(graph, origin);

        check(!result.hasTour(), "An edgeless graph should have no cycle");
        check(result.evaluatedRoutes().size() == 6, "All six permutations should be recorded");
        for (RouteEvaluation evaluation : result.evaluatedRoutes()) {
            List<City> route = evaluation.route();
            check(closedAtOrigin(route, origin), "A discarded route should still start and end at the origin");
            check(route.size() == cities.size() + 1 && route.containsAll(cities),
                    "A discarded route should list every city once before returning to the origin");
            check(evaluation.cost().isEmpty(), "An invalid route should have no cost");
        }
    }

    private static void checksReturnToOriginIsIncluded() {
        City a = new City("A");
        City b = new City("B");
        City c = new City("C");
        Graph graph = new Graph();
        graph.addCity(a);
        graph.addCity(b);
        graph.addCity(c);
        graph.addEdge(a, b, 2);
        graph.addEdge(b, c, 3);
        graph.addEdge(c, a, 4);

        TspResult result = new DijkstraTsp().solve(graph, a);
        check(result.bestCost().orElseThrow() == 9, "Cycle cost must include return edge");
        check(closedAtOrigin(result.bestRoute(), a), "Cycle must end at the origin");
    }

    private static void checksSingleCityHasZeroCost() {
        City a = new City("A");
        Graph graph = new Graph();
        graph.addCity(a);
        check(new BruteForceTsp().solve(graph, a).bestCost().orElseThrow() == 0,
                "Single city brute force cost should be zero");
        check(new DijkstraTsp().solve(graph, a).bestCost().orElseThrow() == 0,
                "Single city Dijkstra cost should be zero");
    }

    private static void checksDijkstraStateSupportsMoreThanSixtyThreeCities() {
        Graph graph = new Graph();
        City origin = new City("V1");
        City second = new City("V2");
        graph.addCity(origin);
        graph.addCity(second);
        for (int i = 3; i <= 64; i++) graph.addCity(new City("V" + i));
        graph.addEdge(origin, second, 1);

        TspResult result = new DijkstraTsp().solve(graph, origin);
        check(!result.hasTour(), "Dijkstra should handle a 64-city graph without an artificial mask limit");
    }

    private static Graph professorGraph() {
        City v1 = new City("V1");
        City v2 = new City("V2");
        City v3 = new City("V3");
        City v4 = new City("V4");
        Graph graph = new Graph();
        for (City city : List.of(v1, v2, v3, v4)) graph.addCity(city);
        graph.addEdge(v1, v2, 1);
        graph.addEdge(v1, v3, 4);
        graph.addEdge(v1, v4, 6);
        graph.addEdge(v2, v3, 2);
        graph.addEdge(v2, v4, 3);
        graph.addEdge(v3, v4, 3);
        return graph;
    }

    private static boolean closedAtOrigin(List<City> route, City origin) {
        return route.size() >= 2 && route.get(0).equals(origin)
                && route.get(route.size() - 1).equals(origin);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
