package br.edu.ifrn.rotaminima.service;

import br.edu.ifrn.rotaminima.algorithm.*;
import br.edu.ifrn.rotaminima.model.*;

public final class RoutePlanningService {
  public DijkstraResult shortestPath(Graph graph, int source) {
    return new DijkstraSolver().solve(graph, source, true);
  }

  public TourResult visitingRoute(Graph graph, int source) {
    return new BruteForceTspSolver().solve(graph, source);
  }
}
