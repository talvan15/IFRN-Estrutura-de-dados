package br.edu.ifrn.rotaminima.model;

public final class DijkstraStep {
  private final int processed;
  private final double[] distances;
  private final int[] predecessors;
  private final boolean[] finalized;

  public DijkstraStep(int processed, double[] distances, int[] predecessors, boolean[] finalized) {
    this.processed = processed;
    this.distances = distances.clone();
    this.predecessors = predecessors.clone();
    this.finalized = finalized.clone();
  }

  public int processed() {
    return processed;
  }

  public double[] distances() {
    return distances.clone();
  }

  public int[] predecessors() {
    return predecessors.clone();
  }

  public boolean[] finalized() {
    return finalized.clone();
  }
}
