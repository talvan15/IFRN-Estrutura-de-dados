package org.example.algorithm;

import org.example.model.City;

import java.util.List;
import java.util.OptionalLong;

/** One route considered by an algorithm; an empty cost means a required road is missing. */
public record RouteEvaluation(List<City> route, OptionalLong cost) {
    public RouteEvaluation {
        route = List.copyOf(route);
    }
}
