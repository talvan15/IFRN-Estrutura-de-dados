package br.edu.ifrn.rotaminima.algorithm;

import br.edu.ifrn.rotaminima.model.*;
import java.util.*;

public final class BruteForceTspSolver {
  public TourResult solve(Graph graph, int source) {
    int n = graph.size();
    if (n < 3 || n > 9)
      throw new IllegalArgumentException("A rota de visitas exige de 3 a 9 cidades.");
    graph.city(source);
    DijkstraResult[] paths = new DijkstraResult[n];
    double[][] distances = new double[n][];
    DijkstraSolver solver = new DijkstraSolver();
    // Fecho de menores distâncias; estes cálculos internos dispensam snapshots.
    for (int i = 0; i < n; i++) {
      paths[i] = solver.solve(graph, i, false);
      distances[i] = paths[i].distances();
    }
    for (double d : distances[source])
      if (!Double.isFinite(d))
        return new TourResult(
            false, graph.cities(), List.of(), List.of(), Double.POSITIVE_INFINITY, 0);
    Search search = new Search(distances, source);
    search.enumerate(1);
    List<Integer> order = new ArrayList<>();
    for (int v : search.best) order.add(v);
    order.add(source);
    List<Integer> expanded = new ArrayList<>();
    expanded.add(source);
    for (int i = 1; i < order.size(); i++) {
      List<Integer> leg = paths[order.get(i - 1)].pathTo(order.get(i));
      // A cidade de junção já é o último vértice do trecho anterior.
      expanded.addAll(leg.subList(1, leg.size()));
    }
    double expandedCost = 0;
    for (int i = 1; i < expanded.size(); i++)
      expandedCost += graph.weight(expanded.get(i - 1), expanded.get(i));
    if (!Double.isFinite(expandedCost)
        || Math.abs(expandedCost - search.bestCost) > 1e-9 * Math.max(1, search.bestCost))
      throw new IllegalStateException("Custo expandido inconsistente.");
    return new TourResult(true, graph.cities(), order, expanded, search.bestCost, search.count);
  }

  private static final class Search {
    final double[][] distances;
    final int[] order;
    final boolean[] used;
    int[] best;
    double bestCost = Double.POSITIVE_INFINITY;
    long count;

    Search(double[][] distances, int source) {
      this.distances = distances;
      order = new int[distances.length];
      used = new boolean[order.length];
      order[0] = source;
      used[source] = true;
    }

    void enumerate(int depth) {
      if (depth == order.length) {
        count++;
        double cost = 0;
        for (int i = 1; i < order.length; i++) cost += distances[order[i - 1]][order[i]];
        // A rota é fechada: o retorno também participa da comparação.
        cost += distances[order[order.length - 1]][order[0]];
        if (!Double.isFinite(cost))
          throw new ArithmeticException("Custo excede a capacidade numérica de double.");
        if (cost < bestCost) {
          bestCost = cost;
          best = order.clone();
        }
        return;
      }
      // Backtracking: reutiliza o vetor de trabalho e visita índices em ordem crescente.
      for (int v = 0; v < order.length; v++)
        if (!used[v]) {
          used[v] = true;
          order[depth] = v;
          enumerate(depth + 1);
          used[v] = false;
        }
    }
  }
}
