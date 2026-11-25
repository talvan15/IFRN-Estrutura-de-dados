package org.example.model;

import java.util.Objects;

/** A city in the travel graph, identified by its display name. */
public record City(String name) {
    public City {
        Objects.requireNonNull(name, "name");
        if (name.isBlank()) {
            throw new IllegalArgumentException("City name cannot be blank");
        }
    }
}
