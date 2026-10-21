package br.edu.ifrn.rotaminima.model;

public record City(int id, String name, double x, double y) {
  public City {
    if (id < 0 || name == null || name.isBlank() || !Double.isFinite(x) || !Double.isFinite(y))
      throw new IllegalArgumentException("Cidade inválida.");
  }

  @Override
  public String toString() {
    return name;
  }
}
