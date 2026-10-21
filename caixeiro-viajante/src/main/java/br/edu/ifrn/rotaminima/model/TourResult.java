package br.edu.ifrn.rotaminima.model;

import java.util.*;

public record TourResult(
    boolean possible,
    List<City> cities,
    List<Integer> plannedOrder,
    List<Integer> expandedPath,
    double cost,
    long evaluatedOrders) {
  public TourResult {
    cities = List.copyOf(cities);
    plannedOrder = List.copyOf(plannedOrder);
    expandedPath = List.copyOf(expandedPath);
  }
}
