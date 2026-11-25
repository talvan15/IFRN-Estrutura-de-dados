package org.example.model;

import java.util.Objects;

/** An undirected weighted road between two distinct cities. */
public record Edge(City first, City second, int weight) {
    public Edge {
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");
        if (first.equals(second)) {
            throw new IllegalArgumentException("A road must connect two distinct cities");
        }
        if (weight < 0) {
            throw new IllegalArgumentException("Edge weight must be non-negative");
        }
    }

    public boolean connects(City city) {
        return first.equals(city) || second.equals(city);
    }

    public City other(City city) {
        if (first.equals(city)) {
            return second;
        }
        if (second.equals(city)) {
            return first;
        }
        throw new IllegalArgumentException("City is not an endpoint of this road");
    }
}
