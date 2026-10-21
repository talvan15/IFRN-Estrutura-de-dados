package br.edu.ifrn.rotaminima.model;

import java.util.*;

public final class DijkstraResult {
  private final int source;
  private final List<City> cities;
  private final double[] distances;
  private final int[] predecessors;
  private final List<DijkstraStep> steps;

  public DijkstraResult(
      int source,
      List<City> cities,
      double[] distances,
      int[] predecessors,
      List<DijkstraStep> steps) {
    this.source = source;
    this.cities = List.copyOf(cities);
    this.distances = distances.clone();
    this.predecessors = predecessors.clone();
    this.steps = List.copyOf(steps);
  }

  public List<City> cities() {
    return cities;
  }

  public double distanceTo(int target) {
    return distances[target];
  }

  public double[] distances() {
    return distances.clone();
  }

  public int[] predecessors() {
    return predecessors.clone();
  }

  public List<DijkstraStep> steps() {
    return steps;
  }

  public boolean reachable(int target) {
    return Double.isFinite(distances[target]);
  }

  public List<Integer> pathTo(int target) {
    if (!reachable(target)) return List.of();
    List<Integer> path = new ArrayList<>();
    for (int v = target; v != -1; v = predecessors[v]) {
      path.add(v);
      if (v == source) break;
    }
    Collections.reverse(path);
    return List.copyOf(path);
  }
}
