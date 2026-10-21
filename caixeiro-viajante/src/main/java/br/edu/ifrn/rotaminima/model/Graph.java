package br.edu.ifrn.rotaminima.model;

import java.util.*;

public final class Graph {
  private final List<City> cities = new ArrayList<>();
  private double[][] weights = new double[0][0];

  public int size() {
    return cities.size();
  }

  public List<City> cities() {
    return List.copyOf(cities);
  }

  public City city(int index) {
    return cities.get(index);
  }

  public void addCity(String name, double x, double y) {
    City city = new City(size(), name, x, y);
    int n = size();
    double[][] next = new double[n + 1][n + 1];
    for (int i = 0; i <= n; i++) {
      Arrays.fill(next[i], Double.POSITIVE_INFINITY);
      next[i][i] = 0;
    }
    for (int i = 0; i < n; i++) System.arraycopy(weights[i], 0, next[i], 0, n);
    cities.add(city);
    weights = next;
  }

  public void setEdge(int a, int b, double weight) {
    checkPair(a, b);
    if (!Double.isFinite(weight) || weight < 0)
      throw new IllegalArgumentException("O peso deve ser finito e não negativo.");
    weights[a][b] = weights[b][a] = weight;
  }

  public void removeEdge(int a, int b) {
    checkPair(a, b);
    weights[a][b] = weights[b][a] = Double.POSITIVE_INFINITY;
  }

  private void checkPair(int a, int b) {
    city(a);
    city(b);
    if (a == b) throw new IllegalArgumentException("Laços não são permitidos.");
  }

  public double weight(int a, int b) {
    return weights[a][b];
  }

  public double[][] adjacencyMatrix() {
    return Arrays.stream(weights).map(double[]::clone).toArray(double[][]::new);
  }

  public Graph copy() {
    Graph g = new Graph();
    g.cities.addAll(cities);
    g.weights = adjacencyMatrix();
    return g;
  }

  public void validate() {
    for (int i = 0; i < size(); i++)
      for (int j = 0; j < size(); j++) {
        double w = weights[i][j];
        if (Double.isNaN(w) || w < 0 || w != weights[j][i] || (i == j && w != 0))
          throw new IllegalArgumentException("Matriz inválida ou assimétrica.");
      }
  }

  public static Graph fromMatrix(List<City> cities, double[][] matrix) {
    Graph g = new Graph();
    g.cities.addAll(cities);
    if (matrix.length != cities.size()) throw new IllegalArgumentException("Dimensão inválida.");
    for (int i = 0; i < cities.size(); i++)
      if (cities.get(i).id() != i || matrix[i].length != cities.size())
        throw new IllegalArgumentException("Índices ou dimensão inválidos.");
    g.weights = Arrays.stream(matrix).map(double[]::clone).toArray(double[][]::new);
    g.validate();
    return g;
  }
}
