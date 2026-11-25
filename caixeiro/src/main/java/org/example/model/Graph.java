package org.example.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;

/** Mutable, undirected weighted graph of cities and roads. */
public final class Graph {
    private final Map<String, City> cities = new LinkedHashMap<>();
    private final Map<City, Map<City, Integer>> adjacency = new LinkedHashMap<>();
    private final List<Edge> edges = new ArrayList<>();

    public void addCity(City city) {
        Objects.requireNonNull(city, "city");
        if (cities.containsKey(city.name())) {
            throw new IllegalArgumentException("City already exists: " + city.name());
        }
        cities.put(city.name(), city);
        adjacency.put(city, new LinkedHashMap<>());
    }

    public void addEdge(City first, City second, int weight) {
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");
        City storedFirst = requireCity(first);
        City storedSecond = requireCity(second);
        Edge edge = new Edge(storedFirst, storedSecond, weight);
        if (adjacency.get(storedFirst).containsKey(storedSecond)) {
            throw new IllegalArgumentException("Road already exists between "
                    + first.name() + " and " + second.name());
        }
        adjacency.get(storedFirst).put(storedSecond, weight);
        adjacency.get(storedSecond).put(storedFirst, weight);
        edges.add(edge);
    }

    public List<City> cities() {
        return List.copyOf(cities.values());
    }

    /** Returns each undirected road once, in insertion order. */
    public List<Edge> edges() {
        return List.copyOf(edges);
    }

    /** Returns neighboring cities and their direct road weights. */
    public Map<City, Integer> neighborsOf(City city) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(adjacency.get(requireCity(city))));
    }

    public OptionalInt weightBetween(City first, City second) {
        City storedFirst = requireCity(first);
        City storedSecond = requireCity(second);
        Integer weight = adjacency.get(storedFirst).get(storedSecond);
        return weight == null ? OptionalInt.empty() : OptionalInt.of(weight);
    }

    public boolean containsCity(City city) {
        return city != null && cities.containsKey(city.name());
    }

    private City requireCity(City city) {
        Objects.requireNonNull(city, "city");
        City storedCity = cities.get(city.name());
        if (storedCity == null) {
            throw new IllegalArgumentException("City is not in this graph: " + city.name());
        }
        return storedCity;
    }
}
