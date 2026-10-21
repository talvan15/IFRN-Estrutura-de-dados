package br.edu.ifrn.rotaminima;

import static org.junit.jupiter.api.Assertions.*;

import br.edu.ifrn.rotaminima.algorithm.*;
import br.edu.ifrn.rotaminima.model.*;
import br.edu.ifrn.rotaminima.service.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class AlgorithmTest {
  private final DijkstraSolver dijkstra = new DijkstraSolver();
  private final BruteForceTspSolver tsp = new BruteForceTspSolver();

  @Test
  void indirectPathAndPredecessors() {
    var r = dijkstra.solve(GraphExamples.create(1), 0, true);
    assertEquals(List.of(0, 1, 2, 3), r.pathTo(3));
    assertArrayEquals(new int[] {-1, 0, 1, 2}, r.predecessors());
    assertArrayEquals(new double[] {0, 2, 5, 6}, r.distances(), 1e-9);
  }

  @Test
  void sameSource() {
    var r = dijkstra.solve(GraphExamples.create(1), 2, true);
    assertEquals(0, r.distanceTo(2), 1e-9);
    assertEquals(List.of(2), r.pathTo(2));
  }

  @Test
  void unreachable() {
    var r = dijkstra.solve(GraphExamples.create(2), 0, true);
    assertFalse(r.reachable(3));
    assertTrue(r.pathTo(3).isEmpty());
    assertEquals(2, r.steps().size());
  }

  @Test
  void zeroWeight() {
    Graph g = GraphExamples.create(3);
    g.setEdge(0, 1, 0);
    var r = dijkstra.solve(g, 0, true);
    assertEquals(1, r.distanceTo(2), 1e-9);
    assertEquals(List.of(0, 1, 2), r.pathTo(2));
  }

  @Test
  void independentSnapshots() {
    Graph g = GraphExamples.create(1);
    var r = dijkstra.solve(g, 0, true);
    var first = r.steps().get(0);
    assertEquals(10, first.distances()[2], 1e-9);
    assertEquals(5, r.steps().get(1).distances()[2], 1e-9);
    first.distances()[1] = 999;
    first.predecessors()[1] = 99;
    first.finalized()[0] = false;
    g.setEdge(0, 1, 100);
    g.addCity("E", 0, 0);
    assertEquals(2, first.distances()[1], 1e-9);
    assertEquals(0, first.predecessors()[1]);
    assertTrue(first.finalized()[0]);
    assertEquals(4, r.cities().size());
    assertEquals(6, r.distanceTo(3), 1e-9);
    assertTrue(dijkstra.solve(g, 0, false).steps().isEmpty());
  }

  @Test
  void invalidWeights() {
    Graph g = GraphExamples.create(3);
    for (double w :
        new double[] {-1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
      assertThrows(IllegalArgumentException.class, () -> g.setEdge(0, 1, w));
    assertThrows(IllegalArgumentException.class, () -> g.setEdge(0, 0, 1));
  }

  @Test
  void matrixValidationAndCopy() {
    var cities = GraphExamples.create(3).cities();
    assertThrows(
        IllegalArgumentException.class,
        () -> Graph.fromMatrix(cities, new double[][] {{0, 1, 2}, {2, 0, 1}, {2, 1, 0}}));
    assertThrows(
        IllegalArgumentException.class,
        () -> Graph.fromMatrix(cities, new double[][] {{0, -1, 2}, {-1, 0, 1}, {2, 1, 0}}));
    Graph g = GraphExamples.create(3);
    double[][] m = g.adjacencyMatrix();
    m[0][1] = 90;
    assertEquals(1, g.weight(0, 1), 1e-9);
    g.copy().removeEdge(0, 1);
    assertEquals(1, g.weight(0, 1), 1e-9);
  }

  @Test
  void deterministicTie() {
    Graph g = GraphExamples.create(0);
    g.setEdge(0, 2, 1);
    var r = dijkstra.solve(g, 0, true);
    assertEquals(1, r.steps().get(1).processed());
  }

  @Test
  void suppliedExampleHasMetricOptimumTen() {
    Graph g = GraphExamples.create(0);
    var r = tsp.solve(g, 0);
    assertTrue(r.possible());
    assertEquals(10, r.cost(), 1e-9);
    assertEquals(6, r.evaluatedOrders());
    assertEquals(0, r.plannedOrder().get(0));
    assertEquals(0, r.plannedOrder().get(4));
    assertEquals(4, new HashSet<>(r.plannedOrder().subList(0, 4)).size());
    checkExpanded(g, r);
  }

  @Test
  void repeatedVertexAndReturn() {
    Graph g = GraphExamples.create(3);
    var r = tsp.solve(g, 0);
    assertEquals(4, r.cost(), 1e-9);
    assertEquals(List.of(0, 1, 2, 0), r.plannedOrder());
    assertEquals(List.of(0, 1, 2, 1, 0), r.expandedPath());
    assertEquals(2, r.evaluatedOrders());
    checkExpanded(g, r);
  }

  @Test
  void disconnectedTour() {
    var r = tsp.solve(GraphExamples.create(2), 0);
    assertFalse(r.possible());
    assertTrue(r.expandedPath().isEmpty());
    assertEquals(0, r.evaluatedOrders());
  }

  @Test
  void cityLimitsAndFactorial() {
    Graph g = new Graph();
    for (int i = 0; i < 10; i++) {
      g.addCity("C" + i, i, 0);
      if (i == 1) assertThrows(IllegalArgumentException.class, () -> tsp.solve(g, 0));
    }
    assertThrows(IllegalArgumentException.class, () -> tsp.solve(g, 0));
    Graph nine = new Graph();
    for (int i = 0; i < 9; i++) {
      nine.addCity("C" + i, i, 0);
      for (int j = 0; j < i; j++) nine.setEdge(i, j, 1);
    }
    var r = tsp.solve(nine, 4);
    assertEquals(40320, r.evaluatedOrders());
    assertEquals(9, r.cost(), 1e-9);
    checkExpanded(nine, r);
  }

  @Test
  void resultImmutability() {
    Graph g = GraphExamples.create(0);
    var r = tsp.solve(g, 0);
    g.addCity("E", 0, 0);
    assertEquals(4, r.cities().size());
    assertThrows(UnsupportedOperationException.class, () -> r.expandedPath().add(9));
  }

  @Test
  void zeroCostTourAndFractionalWeights() {
    Graph graph = GraphExamples.create(3);
    graph.setEdge(0, 1, 0);
    graph.setEdge(1, 2, 0);
    TourResult zero = tsp.solve(graph, 0);
    assertEquals(0, zero.cost(), 1e-12);
    assertEquals(List.of(0, 1, 2, 1, 0), zero.expandedPath());
    checkExpanded(graph, zero);
    graph.setEdge(0, 1, 0.1);
    graph.setEdge(1, 2, 0.2);
    TourResult fractional = tsp.solve(graph, 0);
    assertEquals(0.6, fractional.cost(), 1e-12);
    checkExpanded(graph, fractional);
  }

  private void checkExpanded(Graph g, TourResult r) {
    double sum = 0;
    for (int i = 1; i < r.expandedPath().size(); i++) {
      double w = g.weight(r.expandedPath().get(i - 1), r.expandedPath().get(i));
      assertTrue(Double.isFinite(w));
      sum += w;
    }
    assertEquals(r.cost(), sum, 1e-9);
  }
}
