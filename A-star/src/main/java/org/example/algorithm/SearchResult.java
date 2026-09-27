package org.example.algorithm;

import org.example.model.Position;

import java.util.List;

public class SearchResult {

    private final String algorithmName;
    private final boolean found;
    private final List<Position> path;
    private final List<SearchStep> steps;

    private final int exploredNodes;
    private final double pathCost;
    private final double executionTimeMs;

    public SearchResult(
            String algorithmName,
            boolean found,
            List<Position> path,
            List<SearchStep> steps,
            int exploredNodes,
            double pathCost,
            double executionTimeMs
    ) {
        this.algorithmName = algorithmName;
        this.found = found;
        this.path = path;
        this.steps = steps;
        this.exploredNodes = exploredNodes;
        this.pathCost = pathCost;
        this.executionTimeMs = executionTimeMs;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public boolean isFound() {
        return found;
    }

    public List<Position> getPath() {
        return path;
    }

    public List<SearchStep> getSteps() {
        return steps;
    }

    public int getExploredNodes() {
        return exploredNodes;
    }

    public double getPathCost() {
        return pathCost;
    }

    public double getExecutionTimeMs() {
        return executionTimeMs;
    }
}