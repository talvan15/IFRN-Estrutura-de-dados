package br.edu.ifrn.rotaminima.service;

import br.edu.ifrn.rotaminima.model.*;

public final class GraphExamples {
  public static final String[] NAMES = {
    "Quatro cidades — ótimo real 10 (enunciado: 11)",
    "Dijkstra — caminho indireto",
    "Grafo desconectado",
    "Repetição no trajeto"
  };

  public static Graph create(int example) {
    Graph g = new Graph();
    int n = example == 3 ? 3 : 4;
    double[][] positions = {{180, 180}, {750, 180}, {750, 600}, {180, 600}};
    for (int i = 0; i < n; i++) g.addCity("" + (char) ('A' + i), positions[i][0], positions[i][1]);
    double[][] edges =
        switch (example) {
          case 0 ->
              new double[][] {{0, 1, 1}, {0, 2, 4}, {0, 3, 6}, {1, 2, 2}, {1, 3, 3}, {2, 3, 3}};
          case 1 -> new double[][] {{0, 1, 2}, {1, 2, 3}, {0, 2, 10}, {2, 3, 1}, {1, 3, 8}};
          case 2 -> new double[][] {{0, 1, 2}, {2, 3, 3}};
          case 3 -> new double[][] {{0, 1, 1}, {1, 2, 1}};
          default -> throw new IllegalArgumentException("Exemplo inválido.");
        };
    for (double[] e : edges) g.setEdge((int) e[0], (int) e[1], e[2]);
    return g;
  }
}
