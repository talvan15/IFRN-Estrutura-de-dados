package br.edu.ifrn.rotaminima.algorithm;

import br.edu.ifrn.rotaminima.model.*;
import java.util.*;

public final class DijkstraSolver {
  public DijkstraResult solve(Graph graph, int source, boolean recordSteps) {
    graph.validate();
    graph.city(source);
    int n = graph.size();
    double[] distances = new double[n];
    Arrays.fill(distances, Double.POSITIVE_INFINITY);
    distances[source] = 0;
    int[] predecessors = new int[n];
    Arrays.fill(predecessors, -1);
    boolean[] finalized = new boolean[n];
    List<DijkstraStep> steps = new ArrayList<>();
    for (int iteration = 0; iteration < n; iteration++) {
      int current = -1;
      for (int v = 0; v < n; v++)
        if (!finalized[v]
            && Double.isFinite(distances[v])
            && (current == -1 || distances[v] < distances[current])) current = v;
      if (current == -1) break;
      finalized[current] = true;
      for (int v = 0; v < n; v++)
        if (!finalized[v] && Double.isFinite(graph.weight(current, v))) {
          double candidate = distances[current] + graph.weight(current, v);
          if (!Double.isFinite(candidate))
            throw new ArithmeticException("Os pesos excedem a capacidade numérica de double.");
          // Empates não alteram predecessores, inclusive com arestas de peso zero.
          if (candidate < distances[v]) {
            distances[v] = candidate;
            predecessors[v] = current;
          }
        }
      // O construtor copia os vetores antes da próxima iteração.
      if (recordSteps) steps.add(new DijkstraStep(current, distances, predecessors, finalized));
    }
    return new DijkstraResult(source, graph.cities(), distances, predecessors, steps);
  }
}
